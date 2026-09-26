// Core HTTP Fetch Client for NETSCOPE REST API

async function request(url, options = {}) {
  const defaultHeaders = {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  };

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers
    }
  };

  if (options.body && typeof options.body !== 'string' && !(options.body instanceof FormData)) {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const errorText = await response.text();
      let errorMessage = `HTTP Error ${response.status}: ${response.statusText}`;
      try {
        const errorJson = JSON.parse(errorText);
        if (errorJson.message) errorMessage = errorJson.message;
      } catch (_) {}
      
      const error = new Error(errorMessage);
      error.status = response.status;
      throw error;
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  } catch (err) {
    if (err.name === 'TypeError' && err.message.includes('fetch')) {
      const connError = new Error('Unable to connect to Spring Boot backend server.');
      connError.isConnectionLoss = true;
      throw connError;
    }
    throw err;
  }
}

export const api = {
  get: (url, options) => request(url, { ...options, method: 'GET' }),
  post: (url, body, options) => request(url, { ...options, method: 'POST', body }),
  put: (url, body, options) => request(url, { ...options, method: 'PUT', body }),
  patch: (url, body, options) => request(url, { ...options, method: 'PATCH', body }),
  delete: (url, options) => request(url, { ...options, method: 'DELETE' })
};
