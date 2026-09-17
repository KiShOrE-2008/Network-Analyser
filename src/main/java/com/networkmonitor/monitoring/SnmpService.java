package com.networkmonitor.monitoring;

import com.networkmonitor.dto.SnmpMetricDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class SnmpService {
    private static final Logger log=LoggerFactory.getLogger(SnmpService.class);
    private static final OID UPTIME=new OID("1.3.6.1.2.1.1.3.0");
    private static final OID IF_NUMBER=new OID("1.3.6.1.2.1.2.1.0");
    private static final OID CPU=new OID("1.3.6.1.2.1.25.3.3.1.2.1");
    private static final OID STORAGE_DESCR=new OID("1.3.6.1.2.1.25.2.3.1.3");
    private static final OID STORAGE_UNIT=new OID("1.3.6.1.2.1.25.2.3.1.4");
    private static final OID STORAGE_SIZE=new OID("1.3.6.1.2.1.25.2.3.1.5");
    private static final OID STORAGE_USED=new OID("1.3.6.1.2.1.25.2.3.1.6");

    public SnmpMetricDto querySnmpMetrics(Long deviceId,String ipAddress,String community){
        String comm=community!=null&&!community.isBlank()?community:"public"; Snmp snmp=null;
        try{
            Address addr=GenericAddress.parse("udp:"+ipAddress+"/161"); if(addr==null)return unavailable(deviceId,ipAddress,comm,"Invalid SNMP address");
            DefaultUdpTransportMapping transport=new DefaultUdpTransportMapping(); snmp=new Snmp(transport); transport.listen();
            CommunityTarget<Address> target=new CommunityTarget<>(); target.setCommunity(new OctetString(comm)); target.setAddress(addr); target.setRetries(0); target.setTimeout(1500); target.setVersion(SnmpConstants.version2c);
            PDU pdu=new PDU(); pdu.setType(PDU.GET); pdu.add(new VariableBinding(UPTIME));pdu.add(new VariableBinding(IF_NUMBER));pdu.add(new VariableBinding(CPU));
            ResponseEvent<?> event=snmp.send(pdu,target); if(event==null||event.getResponse()==null)return unavailable(deviceId,ipAddress,comm,"SNMP agent did not respond");
            PDU rp=event.getResponse(); if(rp.getErrorStatus()!=PDU.noError)return unavailable(deviceId,ipAddress,comm,rp.getErrorStatusText());
            Long uptime=null; Double cpu=null; Integer ifs=null;
            for(VariableBinding vb:rp.getVariableBindings()){OID o=vb.getOid(); Variable v=vb.getVariable(); if(o.equals(UPTIME))uptime=v.toLong()/100L; else if(o.equals(IF_NUMBER))ifs=v.toInt(); else if(o.equals(CPU))cpu=(double)v.toInt();}
            Double memory=readPhysicalMemoryUsage(snmp,target);
            return new SnmpMetricDto(deviceId,ipAddress,uptime,cpu,memory,ifs,comm,true,null);
        }catch(Exception e){log.debug("SNMP query to {} failed: {}",ipAddress,e.getMessage());return unavailable(deviceId,ipAddress,comm,"SNMP query failed: "+e.getMessage());}
        finally{if(snmp!=null)try{snmp.close();}catch(IOException ignored){}}
    }

    private Double readPhysicalMemoryUsage(Snmp snmp,CommunityTarget<Address> target){
        try{
            PDU next=new PDU();next.setType(PDU.GETNEXT);next.add(new VariableBinding(STORAGE_DESCR));
            ResponseEvent<?> ev=snmp.send(next,target); if(ev==null||ev.getResponse()==null)return null;
            VariableBinding vb=ev.getResponse().get(0); if(vb==null||vb.getVariable()==null)return null; String descr=vb.getVariable().toString().toLowerCase();
            if(!(descr.contains("physical")&&descr.contains("memory")))return null;
            OID idx=vb.getOid(); idx=new OID(idx.toString());
            OID unit=new OID(STORAGE_UNIT.toString()+idx.toString().substring(STORAGE_DESCR.size()));
            OID size=new OID(STORAGE_SIZE.toString()+idx.toString().substring(STORAGE_DESCR.size()));
            OID used=new OID(STORAGE_USED.toString()+idx.toString().substring(STORAGE_DESCR.size()));
            PDU get=new PDU();get.setType(PDU.GET);get.add(new VariableBinding(unit));get.add(new VariableBinding(size));get.add(new VariableBinding(used));
            ResponseEvent<?> r=snmp.send(get,target); if(r==null||r.getResponse()==null)return null; if(r.getResponse().size()<3)return null;
            long u=r.getResponse().get(0).getVariable().toLong(), s=r.getResponse().get(1).getVariable().toLong(), usedValue=r.getResponse().get(2).getVariable().toLong();
            if(u<=0||s<=0)return null; return Math.max(0,Math.min(100,(usedValue*100.0)/s));
        }catch(Exception e){return null;}
    }
    private SnmpMetricDto unavailable(Long id,String ip,String comm,String error){return new SnmpMetricDto(id,ip,null,null,null,null,comm,false,error);}
}
