-- ==========================================
-- Database SQL Schema for Teenlancer Hub
-- Compatible with PostgreSQL & Supabase
-- ==========================================

-- 1. User Profiles
CREATE TABLE IF NOT EXISTS user_profiles (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    bio TEXT,
    score INTEGER DEFAULT 0,
    streak INTEGER DEFAULT 0,
    rank VARCHAR(100) DEFAULT 'Novice',
    fampay_upi VARCHAR(200),
    fampay_qr_uri TEXT,
    badges TEXT DEFAULT '', -- comma separated
    target_goal_gigs INTEGER DEFAULT 5,
    completed_gigs INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Gigs
CREATE TABLE IF NOT EXISTS gigs (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    budget INTEGER NOT NULL,
    category VARCHAR(100) NOT NULL,
    creator_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    creator_name VARCHAR(150) NOT NULL,
    assignee_id INTEGER REFERENCES user_profiles(id) ON DELETE SET NULL,
    assignee_name VARCHAR(150),
    status VARCHAR(50) DEFAULT 'Open', -- 'Open', 'In_Progress', 'Proof_Submitted', 'Completed'
    payment_proof_uri TEXT,
    created_time BIGINT NOT NULL
);

-- 3. Matchmaking (Co-Founder Posts)
CREATE TABLE IF NOT EXISTS matchmaking_posts (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    role_required VARCHAR(150) NOT NULL,
    creator_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    creator_name VARCHAR(150) NOT NULL,
    created_time BIGINT NOT NULL
);

-- 4. Co-Founder Applications
CREATE TABLE IF NOT EXISTS co_founder_applications (
    id SERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL REFERENCES matchmaking_posts(id) ON DELETE CASCADE,
    post_title VARCHAR(255) NOT NULL,
    applicant_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    applicant_name VARCHAR(150) NOT NULL,
    pitch TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'Pending', -- 'Pending', 'Accepted', 'Declined'
    apply_time BIGINT NOT NULL
);

-- 5. Tech Threads Forum
CREATE TABLE IF NOT EXISTS tech_threads (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    creator_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    creator_name VARCHAR(150) NOT NULL,
    image_uri TEXT,
    created_time BIGINT NOT NULL
);

-- 6. Forum Thread Replies
CREATE TABLE IF NOT EXISTS thread_replies (
    id SERIAL PRIMARY KEY,
    thread_id INTEGER NOT NULL REFERENCES tech_threads(id) ON DELETE CASCADE,
    creator_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    creator_name VARCHAR(150) NOT NULL,
    reply_text TEXT NOT NULL,
    created_time BIGINT NOT NULL
);

-- 7. Ratings and Reviews
CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    reviewer_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    reviewer_name VARCHAR(150) NOT NULL,
    target_user_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    gig_id INTEGER NOT NULL REFERENCES gigs(id) ON DELETE CASCADE,
    gig_title VARCHAR(255) NOT NULL,
    rating REAL NOT NULL,
    comment TEXT NOT NULL,
    created_time BIGINT NOT NULL
);

-- 8. Real-time Multi-device Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    receiver_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    sender_id INTEGER NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    sender_name VARCHAR(150) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'PAYMENT_PROOF', 'COFOUNDER_APPLY', 'FORUM_REPLY', 'STATUS_UPDATE'
    related_id INTEGER NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_time BIGINT NOT NULL
);

-- ==========================================
-- Initial Seeding (Matches internal App DB seed for easy synchronization)
-- ==========================================
INSERT INTO user_profiles (id, username, name, bio, score, streak, rank, fampay_upi, fampay_qr_uri, badges, target_goal_gigs, completed_gigs)
VALUES 
(1, 'aarav_edits', 'Aarav Sharma', '17yo Video editing specialist. Built 3 channels from 0 to 100k subscribers. Works fast, high quality animations.', 1500, 12, 'Expert Editor', 'aarav@fam', 'simulated_qr_aarav', '🔥 10-Day Streak, 🎬 Editor Pro, 👑 Top Earner', 5, 4),
(2, 'riya_codes', 'Riya Patel', '16yo Self-taught Next.js, React, and Kotlin Developer. Hackathon winner. Passionate about building sleek SaaS platforms.', 2500, 5, 'NextJS Wizard', 'riya@fam', 'simulated_qr_riya', '🚀 Innovator, 🤝 Team Player, 💡 Tech Geek', 10, 7),
(3, 'ishaan_designs', 'Ishaan Gupta', '15yo UI/UX enthusiast. Mobile App Designer. Making clean, Material 3 prototypes in Figma. Seeking coders to team up.', 300, 1, 'UI Apprentice', 'ishaan@fam', NULL, '🌱 Newcomer, 🎨 Palette King', 3, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO gigs (id, title, description, budget, category, creator_id, creator_name, status, created_time)
VALUES 
(1, 'Gaming Video Editor Needed', 'Need clean Minecraft gameplay editing. Expect tight transition cuts, sound effects (like funny memes), and stylized captions. Budget ₹500. Expected delivery: 2 days.', 500, 'Video Editing', 2, 'Riya Patel', 'Open', 1716551125000),
(2, 'Build landing page in NextJS', 'Need a single product page designed beautifully with dynamic testimonials and an interactive roadmap block. Budget ₹1200. I can provide the Figma wireframes.', 1200, 'Web Dev', 3, 'Ishaan Gupta', 'Open', 1716551135000),
(3, 'Need Minimalist Brand Logo & Identity', 'Designing a new webapp for teen task tracking. Need a modern logo plus secondary vector assets for icons (SVG form). Budget ₹300.', 300, 'Design', 1, 'Aarav Sharma', 'Open', 1716551145000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO matchmaking_posts (id, title, description, role_required, creator_id, creator_name, created_time)
VALUES
(1, 'React Developer under 18 for School SaaS', 'Building a neat automated homework manager. I already designed the Figma wireframes, but need a developer partner to implement it on the web. Split profits evenly!', 'React Developer', 3, 'Ishaan Gupta', 1716551155000),
(2, 'Need UI/UX designer for Step-Tracker', 'Making an Android application using Room & Compose. Looking for a designer with outstanding attention-to-detail who can output pixel-perfect Material 3 themes.', 'UI/UX Designer', 1, 'Aarav Sharma', 1716551165000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tech_threads (id, title, content, category, creator_id, creator_name, created_time)
VALUES
(1, 'Is Gemini 2.0 Flash the king for hobby bots?', 'Seriously, the response latency is below 300ms, and with free-tier quotas, we can build massive teenage utility bots without ever entering a credit card. Who has tried deploying to Vercel/Android?', 'AI Tools', 3, 'Ishaan Gupta', 1716551175000),
(2, 'Help resolve hydration mismatch in Next.js 🚀', 'I'm rendering client-side streak badges based on local time. In development, it throws a hydration error saying: Text content does not match server-rendered HTML. Is standard suppressHydrationWarning the only way?', 'Bugs', 1, 'Aarav Sharma', 1716551185000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO thread_replies (id, thread_id, creator_id, creator_name, reply_text, created_time)
VALUES
(1, 2, 2, 'Riya Patel', 'The issue occurs because SSR renders a static greeting while the client uses local browser time. Try wrapping your time-based render in a useEffect that triggers after mounting, so it only client-renders!', 1716551195000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO reviews (id, reviewer_id, reviewer_name, target_user_id, gig_id, gig_title, rating, comment, created_time)
VALUES
(1, 2, 'Riya Patel', 1, 1, 'Minecraft Highlight Reel', 5.0, 'Outstanding delivery. Aarav literally built custom transitions from scratch and added hilarious background memes. Highly recommended!', 1716551205000)
ON CONFLICT (id) DO NOTHING;
