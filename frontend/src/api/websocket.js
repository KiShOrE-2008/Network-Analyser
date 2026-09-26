import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscribers = new Map();
  }

  connect(onConnectCallback, onErrorCallback) {
    if (this.client && this.client.active) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws-monitoring'),
      debug: () => {}, // Disable noisy debug logs
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = () => {
      this.connected = true;
      if (onConnectCallback) onConnectCallback();
      // Resubscribe if any callbacks registered
      this.subscribers.forEach((callback, topic) => {
        this.client.subscribe(topic, (message) => {
          try {
            const data = JSON.parse(message.body);
            callback(data);
          } catch (e) {
            console.error('STOMP parse error:', e);
          }
        });
      });
    };

    this.client.onStompError = (frame) => {
      this.connected = false;
      if (onErrorCallback) onErrorCallback(frame);
    };

    this.client.onWebSocketClose = () => {
      this.connected = false;
    };

    this.client.activate();
  }

  subscribe(topic, callback) {
    this.subscribers.set(topic, callback);
    if (this.client && this.client.connected) {
      return this.client.subscribe(topic, (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (e) {
          console.error('STOMP parse error:', e);
        }
      });
    }
    return null;
  }

  unsubscribe(topic) {
    this.subscribers.delete(topic);
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }
}

export const wsService = new WebSocketService();
