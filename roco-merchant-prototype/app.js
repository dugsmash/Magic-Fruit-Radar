/* =========================================================
 * 远行商人闹钟 · 可点击移动端原型 (app.js)
 * 概念原型：许愿单 + 商品到货通知模拟（消息/声音/震动）
 * ========================================================= */
"use strict";

/* ---------- 商品数据（洛克王国 远行商人 风格示例） ---------- */
const ITEMS = [
  { id: "prism",    name: "棱镜球",     emoji: "🔮", rarity: "传说", cat: "道具", price: "128 洛克钻", desc: "传说级道具，远行商人稀有货架才会出现的七彩棱镜，常用于高阶精灵进化。" },
  { id: "blessing", name: "祝福吊坠",   emoji: "📿", rarity: "史诗", cat: "装备", price: "88 洛克钻",  desc: "史诗护身符，佩戴后幸运值大幅提升，掉落率与商人刷新的玄学之选。" },
  { id: "egg",      name: "炫彩精灵蛋", emoji: "🥚", rarity: "传说", cat: "精灵蛋", price: "168 洛克钻", desc: "可能孵化出稀有色精灵！远行商人压轴货，错过要等很久。" },
  { id: "shield",   name: "圣光护盾",   emoji: "🛡️", rarity: "史诗", cat: "护盾", price: "76 洛克钻",  desc: "战斗减伤 20%，PVP 与高难副本必备，商人每轮限量 5 件。" },
  { id: "mystic",   name: "神秘精灵蛋", emoji: "🪺", rarity: "稀有", cat: "精灵蛋", price: "48 洛克钻", desc: "随机孵化普通~稀有精灵，性价比之选。" },
  { id: "star",     name: "星辉宝石",   emoji: "💎", rarity: "稀有", cat: "道具", price: "36 洛克钻",  desc: "强化装备的稀有材料，日常消耗大户。" },
  { id: "timekey",  name: "时空之钥",   emoji: "🗝️", rarity: "史诗", cat: "道具", price: "99 洛克钻",  desc: "开启时空秘境副本的钥匙，一周仅出现一两次。" },
  { id: "feather",  name: "幸运羽毛",   emoji: "🪶", rarity: "优秀", cat: "道具", price: "18 洛克钻",  desc: "提升抓宠成功率 10%，萌新福音。" },
  { id: "berry",    name: "月光浆果",   emoji: "🫐", rarity: "优秀", cat: "道具", price: "12 洛克钻",  desc: "精灵亲密度喂养材料，常驻走量商品。" },
  { id: "scale",    name: "龙鳞碎片",   emoji: "🧩", rarity: "普通", cat: "装备", price: "8 洛克钻",   desc: "合成龙系装备的低阶材料，偶尔顺手补货。" }
];

/* ---------- 状态与持久化 ---------- */
const store = {
  get(k, d) { try { const v = localStorage.getItem("roco_" + k); return v ? JSON.parse(v) : d; } catch (e) { return d; } },
  set(k, v) { try { localStorage.setItem("roco_" + k, JSON.stringify(v)); } catch (e) { /* 忽略 */ } }
};
const state = {
  wishlist: store.get("wishlist", [
    { id: "prism", enabled: true, sound: true, vibrate: true, msg: true, at: Date.now() },
    { id: "blessing", enabled: true, sound: true, vibrate: false, msg: true, at: Date.now() },
    { id: "egg", enabled: true, sound: true, vibrate: true, msg: true, at: Date.now() }
  ]),
  settings: store.get("settings", { sound: "ding", vibrate: "short", classMode: false, server: "魔法学院 · 一区" }),
  history: store.get("history", [])
};
const save = () => { store.set("wishlist", state.wishlist); store.set("settings", state.settings); store.set("history", state.history); };
const $ = (s) => document.querySelector(s);
const itemOf = (id) => ITEMS.find((i) => i.id === id);

/* ---------- 工具 ---------- */
function ck(v) { return v ? "checked" : ""; }
function fmtTime(ts) {
  const d = new Date(ts);
  const p = (n) => String(n).padStart(2, "0");
  return p(d.getHours()) + ":" + p(d.getMinutes()) + ":" + p(d.getSeconds());
}
function toast(msg, ms = 1800) {
  const t = $("#toast");
  t.textContent = msg; t.hidden = false;
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.hidden = true; }, ms);
}
/* 声音：Web Audio 合成（无需音频文件） */
function playChime(kind) {
  if (kind === "none") return;
  try {
    const Ctx = window.AudioContext || window.webkitAudioContext;
    if (!Ctx) return;
    const ctx = new Ctx();
    if (ctx.state === "suspended") ctx.resume();
    const notes = kind === "magic"
      ? [[523.25, 0], [659.25, .09], [783.99, .18], [1046.5, .27]]
      : [[523.25, 0], [783.99, .12]];
    notes.forEach(([f, dt]) => {
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.type = "sine"; o.frequency.value = f;
      o.connect(g); g.connect(ctx.destination);
      g.gain.setValueAtTime(0.0001, ctx.currentTime + dt);
      g.gain.exponentialRampToValueAtTime(.32, ctx.currentTime + dt + .02);
      g.gain.exponentialRampToValueAtTime(.0001, ctx.currentTime + dt + .45);
      o.start(ctx.currentTime + dt); o.stop(ctx.currentTime + dt + .55);
    });
  } catch (e) { /* 音频不可用时静默 */ }
}
/* 震动 */
function vibrate(kind) {
  if (kind === "none") return;
  try {
    if (!navigator.vibrate) return;
    navigator.vibrate(kind === "long" ? [200, 60, 200] : [80, 40, 80]);
  } catch (e) { /* 忽略 */ }
}

/* ---------- 全局开关 ---------- */
function bindGlobalToggles() {
  const s = state.settings;
  $("#gMsg").checked = true; // 消息通知原型中始终可用
  $("#gSound").checked = s.sound !== "none";
  $("#gVibrate").checked = s.vibrate !== "none";
  $("#gClass").checked = !!s.classMode;
  $("#setSound").value = s.sound;
  $("#setVibrate").value = s.vibrate;
  $("#setClass").checked = !!s.classMode;
  $("#setServer").value = s.server;

  $("#gSound").addEventListener("change", (e) => { s.sound = e.target.checked ? "ding" : "none"; save(); });
  $("#gVibrate").addEventListener("change", (e) => { s.vibrate = e.target.checked ? "short" : "none"; save(); });
  $("#gClass").addEventListener("change", (e) => { s.classMode = e.target.checked; save(); });
  $("#setSound").addEventListener("change", (e) => { s.sound = e.target.value; $("#gSound").checked = s.sound !== "none"; save(); });
  $("#setVibrate").addEventListener("change", (e) => { s.vibrate = e.target.value; $("#gVibrate").checked = s.vibrate !== "none"; save(); });
  $("#setClass").addEventListener("change", (e) => { s.classMode = e.target.checked; $("#gClass").checked = s.classMode; save(); });
  $("#setServer").addEventListener("change", (e) => { s.server = e.target.value; save(); toast("区服已切换：" + s.server); });
  $("#btnBind").addEventListener("click", () => toast("演示：账号绑定流程（原型不实际绑定）"));
}

/* ---------- 许愿单渲染 ---------- */
function renderWishlist() {
  const list = $("#wishList");
  if (!state.wishlist.length) {
    list.innerHTML = '<div class="empty"><span class="big">🧞</span>许愿单还是空的<br>点击「＋ 添加」挑选想蹲的商品</div>';
    return;
  }
  list.innerHTML = state.wishlist.map((w) => {
    const it = itemOf(w.id); if (!it) return "";
    const flags = [
      w.msg ? "🔔" : "🚫",
      w.sound ? "🔊" : "",
      w.vibrate ? "📳" : ""
    ].filter(Boolean).join(" ");
    return `<div class="wish-item" data-id="${it.id}">
      <div class="item-icon r-${it.rarity}">${it.emoji}</div>
      <div class="item-info">
        <div class="item-name">${it.name}</div>
        <div class="item-meta">
          <span class="rarity-tag" style="background:var(--r-${it.rarity === "普通" ? "common" : it.rarity === "优秀" ? "good" : it.rarity === "稀有" ? "rare" : it.rarity === "史诗" ? "epic" : "legend"})">${it.rarity}</span>
          <span>${it.price}</span>
          <span class="flag-icons">${flags}</span>
        </div>
      </div>
      <div class="wish-right">
        <label class="switch" title="订阅开关"><input type="checkbox" data-toggle="${it.id}" ${w.enabled ? "checked" : ""}><span class="slider"></span></label>
        <span style="font-size:11px;color:var(--sub)">${w.enabled ? "盯梢中" : "已暂停"}</span>
      </div>
    </div>`;
  }).join("");

  // 卡片点击 → 详情弹层
  list.querySelectorAll(".wish-item").forEach((el) => el.addEventListener("click", (e) => {
    if (e.target.closest(".switch")) return;
    openItemSheet(el.dataset.id);
  }));
  // 订阅开关
  list.querySelectorAll("[data-toggle]").forEach((sw) => sw.addEventListener("change", (e) => {
    const w = state.wishlist.find((x) => x.id === e.target.dataset.toggle);
    if (w) { w.enabled = e.target.checked; save(); renderWishlist(); toast(w.enabled ? "已开启盯梢" : "已暂停盯梢"); }
  }));
}

/* ---------- 图鉴渲染 ---------- */
function renderCatalog(filterR = "全部", kw = "") {
  const grid = $("#catGrid");
  const list = ITEMS.filter((i) => (filterR === "全部" || i.rarity === filterR) && (!kw || i.name.includes(kw)));
  if (!list.length) { grid.innerHTML = '<div class="empty"><span class="big">🔍</span>没有找到匹配的商品</div>'; return; }
  grid.innerHTML = list.map((i) => `<div class="cat-card" data-id="${i.id}">
      <div class="item-icon r-${i.rarity}">${i.emoji}</div>
      <div class="item-name">${i.name}</div>
      <div class="item-meta"><span class="rarity-tag" style="background:var(--r-${i.rarity === "普通" ? "common" : i.rarity === "优秀" ? "good" : i.rarity === "稀有" ? "rare" : i.rarity === "史诗" ? "epic" : "legend"})">${i.rarity}</span> ${i.price}</div>
    </div>`).join("");
  grid.querySelectorAll(".cat-card").forEach((el) => el.addEventListener("click", () => openItemSheet(el.dataset.id)));
}

/* ---------- 记录渲染 ---------- */
function renderHistory() {
  const s = $("#statsRow");
  const h = state.history;
  const hit = h.filter((x) => x.action === "已上线").length;
  const snooze = h.filter((x) => x.action === "稍后提醒").length;
  s.innerHTML = `<div class="stat"><b>${h.length}</b><span>今日提醒</span></div>
    <div class="stat"><b>${hit}</b><span>已上线</span></div>
    <div class="stat"><b>${snooze}</b><span>稍后提醒</span></div>`;
  const list = $("#historyList");
  if (!h.length) { list.innerHTML = '<div class="empty"><span class="big">🌙</span>还没有提醒记录<br>点首页「模拟到货」试试</div>'; return; }
  list.innerHTML = [...h].reverse().slice(0, 50).map((x) => {
    const cls = x.action === "已上线" ? "badge-hit" : x.action === "稍后提醒" ? "badge-snooze" : "badge-miss";
    return `<div class="his-item"><span class="his-icon">${x.emoji}</span>
      <div><div class="his-title">${x.name} · ${x.action}</div>
      <div class="his-time">${fmtTime(x.ts)} · ${x.note || "远行商人到货提醒"}</div></div>
      <span class="his-badge ${cls}">${x.action}</span></div>`;
  }).join("");
}

/* ---------- 弹层 ---------- */
function openSheet(html) {
  $("#sheetBody").innerHTML = html;
  $("#sheetMask").hidden = false;
}
function closeSheet() { $("#sheetMask").hidden = true; }
function openItemSheet(id) {
  const it = itemOf(id); if (!it) return;
  const w = state.wishlist.find((x) => x.id === id);
  const inList = !!w;
  const rColor = "var(--r-" + (it.rarity === "普通" ? "common" : it.rarity === "优秀" ? "good" : it.rarity === "稀有" ? "rare" : it.rarity === "史诗" ? "epic" : "legend") + ")";
  openSheet(`<div style="display:flex;align-items:center;gap:12px;margin-bottom:8px">
      <div class="item-icon r-${it.rarity}" style="width:64px;height:64px;font-size:32px">${it.emoji}</div>
      <div><h3 style="margin:0">${it.name}</h3>
      <div class="item-meta"><span class="rarity-tag" style="background:${rColor}">${it.rarity}</span> ${it.price} · ${it.cat}</div></div>
    </div>
    <p class="desc">${it.desc}</p>
    <div class="row"><span>🔔 消息通知</span><label class="switch"><input type="checkbox" id="iMsg" ${w && w.msg ? "checked" : ""}><span class="slider"></span></label></div>
    <div class="row"><span>🔊 到货声音</span><label class="switch"><input type="checkbox" id="iSound" ${ck(w ? w.sound : true)}><span class="slider"></span></label></div>
    <div class="row"><span>📳 到货震动</span><label class="switch"><input type="checkbox" id="iVib" ${ck(w ? w.vibrate : true)}><span class="slider"></span></label></div>
    <button class="primary" id="sheetAct">${inList ? "✓ 保存许愿设置" : "＋ 加入许愿单"}</button>
    <button class="secondary" id="sheetSim">🎁 模拟该商品到货</button>`);
  const act = () => {
    const msg = $("#iMsg").checked, sound = $("#iSound").checked, vib = $("#iVib").checked;
    let ww = state.wishlist.find((x) => x.id === id);
    if (!ww) { ww = { id, enabled: true, msg, sound, vibrate: vib, at: Date.now() }; state.wishlist.push(ww); }
    else { ww.msg = msg; ww.sound = sound; ww.vibrate = vib; }
    save(); renderWishlist(); renderCatalog(); closeSheet();
    toast(inList ? "许愿设置已保存" : "已加入许愿单 🎉");
  };
  $("#sheetAct").addEventListener("click", act);
  $("#sheetSim").addEventListener("click", () => { closeSheet(); simulateArrival(id); });
}
$("#btnAddWish").addEventListener("click", () => {
  openSheet(`<h3>添加许愿商品</h3><p class="desc">从图鉴挑选商品，或在「图鉴」页浏览更多。</p>
    <div style="display:flex;flex-wrap:wrap;gap:8px" id="quickPick">
      ${ITEMS.map((i) => `<button class="chip" data-q="${i.id}">${i.emoji} ${i.name}</button>`).join("")}
    </div>`);
  $("#quickPick").querySelectorAll("[data-q]").forEach((b) => b.addEventListener("click", () => { closeSheet(); openItemSheet(b.dataset.q); }));
});

/* ---------- 通知模拟 ---------- */
function simulateArrival(id) {
  const w = state.wishlist.find((x) => x.id === id) || { msg: true, sound: true, vibrate: true };
  const it = itemOf(id);
  const s = state.settings;
  const muted = s.classMode;
  const countdown = 8 + Math.floor(Math.random() * 17); // 8~24 分钟窗口
  const stock = 1 + Math.floor(Math.random() * 6);
  const mm = String(countdown).padStart(2, "0");

  // 通知横幅
  const wrap = $("#notifWrap");
  $("#notifIcon").textContent = it.emoji;
  $("#notifTitle").textContent = "远行商人 · 商品到货";
  $("#notifText").textContent = `「${it.name}」已出现在远行商人货架！剩余窗口 ${mm} 分钟 · 仅剩 ${stock} 件${muted ? " · 上课模式：已静音" : ""}`;
  const acts = $("#notifActions");
  acts.innerHTML = `<button class="nact-primary" id="naGo">⚡ 立即上线</button>
    <button class="nact-ghost" id="naSnooze">稍后提醒</button>
    <button class="nact-silent" id="naIgnore">忽略</button>`;
  wrap.hidden = false;
  $("#notif").classList.toggle("muted", muted);
  $("#naGo").addEventListener("click", () => { hideNotif(); log(it, "已上线"); toast("已打开游戏（原型演示）🎮"); });
  $("#naSnooze").addEventListener("click", () => { hideNotif(); log(it, "稍后提醒"); toast("⏰ 10 分钟后再次提醒"); setTimeout(() => snoozeNotif(it), 8000); });
  $("#naIgnore").addEventListener("click", () => { hideNotif(); log(it, "已忽略"); toast("已忽略本次提醒"); });
  $("#notifClose").onclick = () => { hideNotif(); log(it, "已忽略"); };

  // 声音 + 震动（上课模式静默）
  if (!muted) {
    if (w.sound && s.sound !== "none") playChime(s.sound);
    if (w.vibrate && s.vibrate !== "none") vibrate(s.vibrate);
  }
  log(it, "提醒");
  renderHistory();
}
function hideNotif() { $("#notifWrap").hidden = true; }
function snoozeNotif(it) {
  const s = state.settings;
  $("#notifIcon").textContent = it.emoji;
  $("#notifTitle").textContent = "远行商人 · 稍后提醒";
  $("#notifText").textContent = `「${it.name}」仍在售！剩余窗口约 6 分钟，抓紧上线。`;
  $("#notifActions").innerHTML = `<button class="nact-primary" id="naGo2">⚡ 立即上线</button>`;
  $("#notifWrap").hidden = false;
  $("#naGo2").addEventListener("click", () => { hideNotif(); log(it, "已上线"); toast("已打开游戏（原型演示）🎮"); });
  if (!s.classMode) {
    if (s.sound !== "none") playChime(s.sound);
    if (s.vibrate !== "none") vibrate(s.vibrate);
  }
}
function log(it, action, note) {
  state.history.push({ id: Date.now() + Math.random(), name: it.name, emoji: it.emoji, action, note, ts: Date.now() });
  if (state.history.length > 200) state.history.splice(0, state.history.length - 200);
  save(); renderHistory();
}
$("#btnSimulate").addEventListener("click", () => {
  const enabled = state.wishlist.filter((w) => w.enabled);
  if (!enabled.length) { toast("许愿单里没有启用的商品，先添加吧！"); openItemSheet(ITEMS[0].id); return; }
  simulateArrival(enabled[Math.floor(Math.random() * enabled.length)].id);
});

/* ---------- Tab 切换 ---------- */
const SCREENS = { home: "#screenHome", catalog: "#screenCatalog", history: "#screenHistory", settings: "#screenSettings" };
document.querySelectorAll(".tab").forEach((t) => t.addEventListener("click", () => {
  document.querySelectorAll(".tab").forEach((x) => x.classList.remove("active"));
  t.classList.add("active");
  Object.entries(SCREENS).forEach(([k, sel]) => { $(sel).hidden = k !== t.dataset.tab; });
  if (t.dataset.tab === "history") renderHistory();
  if (t.dataset.tab === "settings") renderInfo();
}));

/* ---------- 状态栏时钟 & 商人状态 ---------- */
function tick() {
  const d = new Date();
  $("#sbTime").textContent = d.getHours() + ":" + String(d.getMinutes()).padStart(2, "0");
}
function refreshMerchant() {
  const r = Math.random();
  const s = r < 0.72 ? "休憩中 · 每轮随机出现" : r < 0.92 ? "🛒 出现中！快去商人那里看看" : "🌙 神秘商人 · 深夜限时";
  $("#merchantStatus").textContent = "🔍 商人状态：" + s;
}
$("#btnRefreshStatus").addEventListener("click", () => { refreshMerchant(); toast("商人状态已刷新"); });

/* ---------- 图鉴事件 ---------- */
$("#catSearch").addEventListener("input", (e) => renderCatalog(currentRarity(), e.target.value.trim()));
document.querySelectorAll("#catRarity .chip").forEach((c) => c.addEventListener("click", () => {
  document.querySelectorAll("#catRarity .chip").forEach((x) => x.classList.remove("active"));
  c.classList.add("active");
  renderCatalog(c.dataset.r, $("#catSearch").value.trim());
}));
function currentRarity() { return document.querySelector("#catRarity .chip.active").dataset.r; }

/* ---------- 设置页情报 ---------- */
const INFO = {
  feasibility: "官方无公开实时 API。数据来源四级方案：①社区API（如「洛克魔法书」付费Key）②页面/协议轮询 ③截图OCR+推送 ④众包上报+手动录入。风险：非官方接口易失效、抓取有封号/合规风险、推送受厂商限制。建议：MVP 用「社区数据源+轮询」验证需求，不押注非官方接口，长期争取官方授权。",
  cost: "一次性：App开发（外包）约 $8,000、UI设计 $800、软著+上架 $200，合计约 $9,000（个人自研可降至 $1,500 内）。月度：轻量云 $30、数据API/抓取节点 $25、推送通道 $10、维护 $50 ≈ $115/月。省钱路径：HTML原型验证 → 免费推送通道（FCM/免费层）→ 自研降本。",
  audience: "目标：16~30 岁玩家（05后~10后新玩家+情怀回归老玩家+代管家长）。痛点：商人出现完全随机、窗口短库存少、游戏内无提醒、攻略站需主动刷新。竞品现状：只有网页查询工具和需自建的 GitHub 提醒器，无应用商店可下载的「心愿单+系统推送」App——明确蓝海。",
  ui: "四 Tab：许愿单/图鉴/提醒记录/我的。核心原则：推送→上线的路径 ≤2 步；打扰分级（普通=横幅，重要=声音+震动）；上课免打扰是刚需；通知带剩余窗口倒计时+库存，制造紧迫感不焦虑。视觉：奶油暖黄+魔法紫+暖橙，大圆角毛绒卡片，稀有度五档渐变（白/绿/蓝/紫/金）并配文字标签。"
};
function renderInfo() {
  $("#infoFeasibility").textContent = INFO.feasibility;
  $("#infoCost").textContent = INFO.cost;
  $("#infoAudience").textContent = INFO.audience;
  $("#infoUi").textContent = INFO.ui;
}

/* ---------- 初始化 ---------- */
$("#btnClearHistory").addEventListener("click", () => { if (confirm("清空所有提醒记录？")) { state.history = []; save(); renderHistory(); toast("记录已清空"); } });
$("#sheetMask").addEventListener("click", (e) => { if (e.target.id === "sheetMask") closeSheet(); });
document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeSheet(); });
tick(); setInterval(tick, 30000);
refreshMerchant();
bindGlobalToggles();
renderWishlist();
renderCatalog();
renderHistory();
