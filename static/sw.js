const CACHE_NAME = 'dodohendo-v1';
const ASSETS = [
  '/static/index.html',
  '/static/styles.css',
  '/static/app.js',
  '/static/manifest.json'
];

self.addEventListener('install', ev => {
  ev.waitUntil(caches.open(CACHE_NAME).then(c => c.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener('fetch', ev => {
  if (ev.request.method !== 'GET') return;
  ev.respondWith(
    caches.match(ev.request).then(res => res || fetch(ev.request).catch(()=>caches.match('/static/index.html')))
  );
});

self.addEventListener('activate', ev => {
  ev.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k=>k!==CACHE_NAME).map(k=>caches.delete(k))))
  );
  self.clients.claim();
});