/**
 * ============================================================================
 * Teenlancer Hub Node.js (Express) & WebSocket Real-Time Backend
 * Supports Supabase / PostgreSQL Client Persistence
 * ============================================================================
 */

require('dotenv').config();
const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const { createClient } = require('@supabase/supabase-js');

const app = express();
const port = process.env.PORT || 3000;

// Enable JSON middleware & CORS
app.use(cors());
app.use(express.json());

// Create public/uploads directory for local image saving fallback
const uploadDir = path.join(__dirname, 'public', 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}
app.use('/uploads', express.static(uploadDir));

// Initialize Supabase Client (if keys are configured)
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;
let supabase = null;

if (supabaseUrl && supabaseKey) {
  supabase = createClient(supabaseUrl, supabaseKey);
  console.log('✅ Supabase Connection initialized successfully!');
} else {
  console.log('⚠️ Supabase credentials missing in .env. Falling back to local in-memory simulation stores for rapid local execution.');
}

// ---------------------------------------------------------
// LOCAL MEMORY STORE (Fallback if Supabase is not configured)
// ---------------------------------------------------------
let localStore = {
  profiles: [
    { id: 1, username: 'aarav_edits', name: 'Aarav Sharma', bio: '17yo Video editing specialist. Built 3 channels from 0 to 100k subscribers. Works fast, high quality animations.', score: 1500, streak: 12, rank: 'Expert Editor', fampayUpi: 'aarav@fam', fampayQrUri: '', badges: '🔥 10-Day Streak, 🎬 Editor Pro, 👑 Top Earner', targetGoalGigs: 5, completedGigs: 4 },
    { id: 2, username: 'riya_codes', name: 'Riya Patel', bio: '16yo Self-taught Next.js, React, and Kotlin Developer. Hackathon winner. Passionate about building sleek SaaS platforms.', score: 2500, streak: 5, rank: 'NextJS Wizard', fampayUpi: 'riya@fam', fampayQrUri: '', badges: '🚀 Innovator, 🤝 Team Player, 💡 Tech Geek', targetGoalGigs: 10, completedGigs: 7 },
    { id: 3, username: 'ishaan_designs', name: 'Ishaan Gupta', bio: '15yo UI/UX enthusiast. Mobile App Designer. Making clean, Material 3 prototypes in Figma. Seeking coders to team up.', score: 300, streak: 1, rank: 'UI Apprentice', fampayUpi: 'ishaan@fam', fampayQrUri: '', badges: '🌱 Newcomer, 🎨 Palette King', targetGoalGigs: 3, completedGigs: 1 }
  ],
  gigs: [
    { id: 1, title: 'Gaming Video Editor Needed', description: 'Need clean Minecraft gameplay editing. Expect tight transition cuts, sound effects (like funny memes), and stylized captions. Budget 500, Expected delivery: 2 days.', budget: 500, category: 'Video Editing', creatorId: 2, creatorName: 'Riya Patel', assigneeId: null, assigneeName: null, status: 'Open', paymentProofUri: null, createdTime: Date.now() - 100000 },
    { id: 2, title: 'Build landing page in NextJS', description: 'Need a single product page designed beautifully with dynamic testimonials and an interactive roadmap block. Budget 1200. I can provide the Figma wireframes.', budget: 1200, category: 'Web Dev', creatorId: 3, creatorName: 'Ishaan Gupta', assigneeId: null, assigneeName: null, status: 'Open', paymentProofUri: null, createdTime: Date.now() - 50000 },
    { id: 3, title: 'Need Minimalist Brand Logo & Identity', description: 'Designing a new webapp for teen task tracking. Need a modern logo plus secondary vector assets for icons (SVG form). Budget 300.', budget: 300, category: 'Design', creatorId: 1, creatorName: 'Aarav Sharma', assigneeId: null, assigneeName: null, status: 'Open', paymentProofUri: null, createdTime: Date.now() - 10000 }
  ],
  matchmaking: [
    { id: 1, title: 'React Developer under 18 for School SaaS', description: 'Building a neat automated homework manager. I already designed the Figma wireframes, but need a developer partner to implement it on the web. Split profits evenly!', roleRequired: 'React Developer', creatorId: 3, creatorName: 'Ishaan Gupta', createdTime: Date.now() },
    { id: 2, title: 'Need UI/UX designer for Step-Tracker', description: 'Making an Android application using Room & Compose. Looking for a designer with outstanding attention-to-detail who can output pixel-perfect Material 3 themes.', roleRequired: 'UI/UX Designer', creatorId: 1, creatorName: 'Aarav Sharma', createdTime: Date.now() }
  ],
  applications: [],
  threads: [
    { id: 1, title: 'Is Gemini 2.0 Flash the king for hobby bots?', content: 'Seriously, the response latency is below 300ms, and with free-tier quotas, we can build massive teenage utility bots without ever entering a credit card. Who has tried deploying to Vercel/Android?', category: 'AI Tools', creatorId: 3, creatorName: 'Ishaan Gupta', imageUri: null, createdTime: Date.now() - 20000 },
    { id: 2, title: 'Help resolve hydration mismatch in Next.js 🚀', content: 'I\'m rendering client-side streak badges based on local time. In development, it throws a hydration error saying: Text content does not match server-rendered HTML. Is standard suppressHydrationWarning the only way?', category: 'Bugs', creatorId: 1, creatorName: 'Aarav Sharma', imageUri: null, createdTime: Date.now() - 10000 }
  ],
  replies: [
    { id: 1, threadId: 2, creatorId: 2, creatorName: 'Riya Patel', replyText: 'The issue occurs because SSR renders a static greeting while the client uses local browser time. Try wrapping your time-based render in a useEffect that triggers after mounting, so it only client-renders!', createdTime: Date.now() }
  ],
  reviews: [
    { id: 1, reviewerId: 2, reviewerName: 'Riya Patel', targetUserId: 1, gigId: 1, gigTitle: 'Minecraft Highlight Reel', rating: 5, comment: 'Outstanding delivery. Aarav literally built custom transitions from scratch and added hilarious background memes.', createdTime: Date.now() }
  ],
  notifications: []
};

// ---------------------------------------------------------
// FILE UPLOADER CONFIGURATION (MULTER)
// ---------------------------------------------------------
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'fampay-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage: storage });

// ---------------------------------------------------------
// WEBSOCKET MAPS FOR REAL-TIME DEVICING
// ---------------------------------------------------------
const clientsMap = new Map(); // Map<UserId, WebSocket>

// WebSocket Helper to push real-time socket events
function pushSocketMessage(userId, action, data) {
  const wsClient = clientsMap.get(parseInt(userId));
  if (wsClient && wsClient.readyState === WebSocket.OPEN) {
    wsClient.send(JSON.stringify({ action, data }));
    console.log(`🌐 Websocket: Dispatched event '${action}' to user ${userId}`);
    return true;
  }
  return false;
}

// ---------------------------------------------------------
// REST API ENDPOINTS
// ---------------------------------------------------------

// --- PROFILES ENDPOINTS ---
app.get('/api/profiles', async (req, res) => {
  if (supabase) {
    const { data, error } = await supabase.from('user_profiles').select('*').order('id', { ascending: true });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.profiles);
});

app.put('/api/profiles/:id', async (req, res) => {
  const profileId = parseInt(req.params.id);
  const { name, bio, score, streak, rank, fampayUpi, fampayQrUri, badges } = req.body;

  if (supabase) {
    const { data, error } = await supabase
      .from('user_profiles')
      .update({ name, bio, score, streak, rank, fampay_upi: fampayUpi, fampay_qr_uri: fampayQrUri, badges })
      .eq('id', profileId)
      .select();
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data[0]);
  }

  const index = localStore.profiles.findIndex(p => p.id === profileId);
  if (index !== -1) {
    localStore.profiles[index] = { ...localStore.profiles[index], ...req.body };
    return res.json(localStore.profiles[index]);
  }
  res.status(404).json({ error: 'Profile not found' });
});

// --- GIGS ENDPOINTS ---
app.get('/api/gigs', async (req, res) => {
  if (supabase) {
    const { data, error } = await supabase.from('gigs').select('*').order('created_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.gigs);
});

app.post('/api/gigs', async (req, res) => {
  const { title, description, budget, category, creatorId, creatorName } = req.body;
  const newGig = {
    title,
    description,
    budget,
    category,
    creator_id: creatorId,
    creator_name: creatorName,
    status: 'Open',
    created_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('gigs').insert([newGig]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.gigs.length + 1;
  const created = { id, ...newGig, creatorId, creatorName, createdTime: newGig.created_time };
  localStore.gigs.unshift(created);
  res.status(201).json(created);
});

app.put('/api/gigs/:id', async (req, res) => {
  const id = parseInt(req.params.id);
  const updates = req.body;

  if (supabase) {
    const { data, error } = await supabase.from('gigs').update(updates).eq('id', id).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data[0]);
  }

  const index = localStore.gigs.findIndex(g => g.id === id);
  if (index !== -1) {
    localStore.gigs[index] = { ...localStore.gigs[index], ...updates };
    return res.json(localStore.gigs[index]);
  }
  res.status(404).json({ error: 'Gig not found' });
});

// Upload proof photo
app.post('/api/gigs/:id/upload-proof', upload.single('screenshot'), async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'Please upload a FamPay QR payment receipt screenshot.' });
  }

  const gigId = parseInt(req.params.id);
  const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;

  // If using Supabase, upload to Supabase Bucket Storage space
  if (supabase && process.env.SUPABASE_BUCKET_NAME) {
    try {
      const fileBuffer = fs.readFileSync(req.file.path);
      const bucketName = process.env.SUPABASE_BUCKET_NAME;
      const fileName = `proofs/${Date.now()}-${req.file.filename}`;

      const { data, error } = await supabase.storage
        .from(bucketName)
        .upload(fileName, fileBuffer, {
          contentType: req.file.mimetype,
          cacheControl: '3600'
        });

      if (!error) {
        // Get public URL
        const { data: { publicUrl } } = supabase.storage.from(bucketName).getPublicUrl(fileName);
        // Clean up temp file
        fs.unlinkSync(req.file.path);
        return res.json({ proofUri: publicUrl });
      } else {
        console.error('Supabase upload error, falling back to local host: ', error.message);
      }
    } catch (err) {
      console.error('Storage bucket error, falling back locally: ', err);
    }
  }

  res.json({ proofUri: fileUrl });
});

// --- MATCHMAKING ENDPOINTS ---
app.get('/api/matchmaking', async (req, res) => {
  if (supabase) {
    const { data, error } = await supabase.from('matchmaking_posts').select('*').order('created_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.matchmaking);
});

app.post('/api/matchmaking', async (req, res) => {
  const { title, description, roleRequired, creatorId, creatorName } = req.body;
  const post = {
    title,
    description,
    role_required: roleRequired,
    creator_id: creatorId,
    creator_name: creatorName,
    created_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('matchmaking_posts').insert([post]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.matchmaking.length + 1;
  const created = { id, ...post, creatorId, creatorName, roleRequired, createdTime: post.created_time };
  localStore.matchmaking.unshift(created);
  res.status(201).json(created);
});

// Co-founder applications
app.get('/api/matchmaking/:postId/applications', async (req, res) => {
  const postId = parseInt(req.params.postId);
  if (supabase) {
    const { data, error } = await supabase.from('co_founder_applications').select('*').eq('post_id', postId).order('apply_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.applications.filter(a => a.postId === postId));
});

app.post('/api/matchmaking/:postId/applications', async (req, res) => {
  const postId = parseInt(req.params.postId);
  const { postTitle, applicantId, applicantName, pitch } = req.body;
  const appModel = {
    post_id: postId,
    post_title: postTitle,
    applicant_id: applicantId,
    applicant_name: applicantName,
    pitch,
    status: 'Pending',
    apply_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('co_founder_applications').insert([appModel]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.applications.length + 1;
  const created = { id, ...appModel, postId, postTitle, applicantId, applicantName, applyTime: appModel.apply_time };
  localStore.applications.unshift(created);
  res.status(201).json(created);
});

// Accept/Decline applications
app.put('/api/applications/:id', async (req, res) => {
  const appId = parseInt(req.params.id);
  const { status } = req.body; // 'Accepted' or 'Declined'

  if (supabase) {
    const { data, error } = await supabase.from('co_founder_applications').update({ status }).eq('id', appId).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data[0]);
  }

  const index = localStore.applications.findIndex(a => a.id === appId);
  if (index !== -1) {
    localStore.applications[index].status = status;
    return res.json(localStore.applications[index]);
  }
  res.status(404).json({ error: 'Application not found' });
});

// --- TECHNICAL DISCUSSION FORUM ---
app.get('/api/forum', async (req, res) => {
  if (supabase) {
    const { data, error } = await supabase.from('tech_threads').select('*').order('created_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.threads);
});

app.post('/api/forum', async (req, res) => {
  const { title, content, category, creatorId, creatorName, imageUri } = req.body;
  const thread = {
    title,
    content,
    category,
    creator_id: creatorId,
    creator_name: creatorName,
    image_uri: imageUri,
    created_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('tech_threads').insert([thread]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.threads.length + 1;
  const created = { id, ...thread, creatorId, creatorName, imageUri, createdTime: thread.created_time };
  localStore.threads.unshift(created);
  res.status(201).json(created);
});

app.get('/api/forum/:threadId/replies', async (req, res) => {
  const threadId = parseInt(req.params.threadId);
  if (supabase) {
    const { data, error } = await supabase.from('thread_replies').select('*').eq('thread_id', threadId).order('created_time', { ascending: true });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.replies.filter(r => r.threadId === threadId));
});

app.post('/api/forum/:threadId/replies', async (req, res) => {
  const threadId = parseInt(req.params.threadId);
  const { creatorId, creatorName, replyText } = req.body;
  const rep = {
    thread_id: threadId,
    creator_id: creatorId,
    creator_name: creatorName,
    reply_text: replyText,
    created_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('thread_replies').insert([rep]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.replies.length + 1;
  const created = { id, ...rep, threadId, creatorId, creatorName, replyText, createdTime: rep.created_time };
  localStore.replies.push(created);
  res.status(201).json(created);
});

// --- RATINGS & REVIEWS ---
app.get('/api/reviews/:userId', async (req, res) => {
  const userId = parseInt(req.params.userId);
  if (supabase) {
    const { data, error } = await supabase.from('reviews').select('*').eq('target_user_id', userId).order('created_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.reviews.filter(r => r.targetUserId === userId));
});

app.post('/api/reviews', async (req, res) => {
  const { reviewerId, reviewerName, targetUserId, gigId, gigTitle, rating, comment } = req.body;
  const review = {
    reviewer_id: reviewerId,
    reviewer_name: reviewerName,
    target_user_id: targetUserId,
    gig_id: gigId,
    gig_title: gigTitle,
    rating,
    comment,
    created_time: Date.now()
  };

  if (supabase) {
    const { data, error } = await supabase.from('reviews').insert([review]).select();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(201).json(data[0]);
  }

  const id = localStore.reviews.length + 1;
  const created = { id, ...review, reviewerId, reviewerName, targetUserId, gigId, gigTitle, rating, comment, createdTime: review.created_time };
  localStore.reviews.unshift(created);
  res.status(201).json(created);
});

// --- REAL-TIME MULTIDEVICE NOTIFICATIONS ---
app.get('/api/notifications/:userId', async (req, res) => {
  const userId = parseInt(req.params.userId);
  if (supabase) {
    const { data, error } = await supabase.from('notifications').select('*').eq('receiver_id', userId).order('created_time', { ascending: false });
    if (error) return res.status(500).json({ error: error.message });
    return res.json(data);
  }
  res.json(localStore.notifications.filter(n => n.receiverId === userId));
});

app.post('/api/notifications', async (req, res) => {
  const { receiverId, senderId, senderName, title, message, type, relatedId } = req.body;
  const notif = {
    receiver_id: receiverId,
    sender_id: senderId,
    sender_name: senderName,
    title,
    message,
    type,
    related_id: relatedId,
    is_read: false,
    created_time: Date.now()
  };

  let savedNotif = null;
  if (supabase) {
    const { data, error } = await supabase.from('notifications').insert([notif]).select();
    if (error) return res.status(500).json({ error: error.message });
    savedNotif = data[0];
  } else {
    const id = localStore.notifications.length + 1;
    savedNotif = { id, ...notif, receiverId, senderId, senderName, relatedId, isRead: false, createdTime: notif.created_time };
    localStore.notifications.unshift(savedNotif);
  }

  // Push immediate real-time socket event!
  pushSocketMessage(receiverId, 'new_notification', savedNotif);

  res.status(201).json(savedNotif);
});

app.put('/api/notifications/:userId/read-all', async (req, res) => {
  const userId = parseInt(req.params.userId);

  if (supabase) {
    const { error } = await supabase.from('notifications').update({ is_read: true }).eq('receiver_id', userId);
    if (error) return res.status(500).json({ error: error.message });
    return res.json({ success: true });
  }

  localStore.notifications.forEach(n => {
    if (n.receiverId === userId) n.isRead = true;
  });
  res.json({ success: true });
});

app.put('/api/notifications/:id/read', async (req, res) => {
  const notifId = parseInt(req.params.id);

  if (supabase) {
    const { error } = await supabase.from('notifications').update({ is_read: true }).eq('id', notifId);
    if (error) return res.status(500).json({ error: error.message });
    return res.json({ success: true });
  }

  const notif = localStore.notifications.find(n => n.id === notifId);
  if (notif) {
    notif.isRead = true;
    return res.json({ success: true });
  }
  res.status(404).json({ error: 'Notification not found' });
});


// ---------------------------------------------------------
// SERVER & WEBSOCKET SETUP
// ---------------------------------------------------------
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

wss.on('connection', (ws) => {
  console.log('🔌 New device connected via WebSocket!');
  let authenticatedUserId = null;

  ws.on('message', (message) => {
    try {
      const parsed = JSON.parse(message);
      
      // Handle user registration of physical or simulated client device
      if (parsed.action === 'register') {
        const userId = parseInt(parsed.userId);
        if (userId) {
          authenticatedUserId = userId;
          clientsMap.set(userId, ws);
          console.log(`👤 Assigned Web Socket context mapping to Active User Profile ID: ${userId}`);
          ws.send(JSON.stringify({ action: 'registered', success: true }));
        }
      }
    } catch (err) {
      console.error('WebSocket message parsing failure: ', err);
    }
  });

  ws.on('close', () => {
    if (authenticatedUserId) {
      clientsMap.delete(authenticatedUserId);
      console.log(`🔌 Web Socket channel decommissioned for User ID: ${authenticatedUserId}`);
    }
  });
});

server.listen(port, () => {
  console.log(`\n==================================================================`);
  console.log(`🚀 Teenlancer Hub Server running live: http://localhost:${port}`);
  console.log(`📡 WebSocket Listener setup and active!`);
  console.log(`==================================================================\n`);
});
