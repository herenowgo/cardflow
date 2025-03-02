// SSE客户端示例代码，展示如何订阅和处理心跳
const userId = 'user123';
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;
const reconnectDelay = 3000; // 3秒
let lastHeartbeatTime = Date.now();
const heartbeatTimeout = 60000; // 60秒

function connectSSE() {
  const eventSource = new EventSource(`/api/stream/subscribe/${userId}`);
  
  // 连接建立
  eventSource.onopen = function() {
    console.log('SSE连接已建立');
    reconnectAttempts = 0; // 重置重连尝试次数
    lastHeartbeatTime = Date.now(); // 初始化心跳时间
  };

  // 接收消息
  eventSource.onmessage = function(event) {
    const data = JSON.parse(event.data);
    
    // 处理心跳消息
    if (data.eventType === 'HEARTBEAT') {
      console.log('收到心跳:', data);
      lastHeartbeatTime = Date.now(); // 更新最近心跳时间
      return;
    }
    
    // 处理其他类型的消息
    console.log('收到消息:', data);
    // 根据不同的eventType处理不同类型的业务消息
  };

  // 处理错误
  eventSource.onerror = function(error) {
    console.error('SSE错误:', error);
    eventSource.close();
    
    // 尝试重连
    if (reconnectAttempts < maxReconnectAttempts) {
      reconnectAttempts++;
      console.log(`尝试重连 (${reconnectAttempts}/${maxReconnectAttempts})...`);
      setTimeout(connectSSE, reconnectDelay);
    } else {
      console.error('达到最大重连次数，停止重连');
    }
  };

  // 设置心跳检测定时器
  const heartbeatChecker = setInterval(() => {
    const now = Date.now();
    if (now - lastHeartbeatTime > heartbeatTimeout) {
      console.warn('心跳超时，重新连接...');
      clearInterval(heartbeatChecker);
      eventSource.close();
      connectSSE();
    }
  }, 5000); // 每5秒检查一次
}

// 启动连接
connectSSE();
