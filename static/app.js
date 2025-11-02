// Basit mobil UI: /chat endpoint'ine POST atar ve config.json içindeki token ile Authorization başlığı gönderir
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('chat-form');
  const input = document.getElementById('message');
  const area = document.getElementById('chat-area');

  let CONFIG = { API_TOKEN: null, API_URL: '/chat' };

  async function loadConfig(){
    try{
      const resp = await fetch('/static/config.json');
      if(resp.ok){
        const j = await resp.json();
        CONFIG = Object.assign(CONFIG, j);
      }
    }catch(e){ /* ignore, use defaults */ }
  }

  function appendMessage(author, text) {
    const p = document.createElement('div');
    p.className = `msg ${author}`;
    p.textContent = text;
    area.appendChild(p);
    area.scrollTop = area.scrollHeight;
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const text = input.value.trim();
    if (!text) return;
    appendMessage('user', text);
    input.value = '';
    appendMessage('pending', 'Gönderiliyor...');

    try {
      const headers = {'Content-Type': 'application/json'};
      if(CONFIG.API_TOKEN) headers['Authorization'] = 'Bearer ' + CONFIG.API_TOKEN;

      const resp = await fetch(CONFIG.API_URL || '/chat', {
        method: 'POST',
        headers,
        body: JSON.stringify({message: text})
      });
      const data = await resp.json();
      const pending = area.querySelector('.pending');
      if (pending) pending.remove();

      if (resp.ok) {
        appendMessage('bot', data.reply || 'Yanıt yok');
      } else {
        appendMessage('bot', 'Hata: ' + (data.error || resp.statusText));
      }
    } catch (err) {
      const pending = area.querySelector('.pending');
      if (pending) pending.remove();
      appendMessage('bot', 'İstek başarısız: ' + err.message);
    }
  });

  // Service worker register (PWA)
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/static/sw.js').catch(()=>{});
  }

  loadConfig();
});