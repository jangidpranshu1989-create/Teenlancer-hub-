# Teenlancer Setup Guide

A lightweight Express backend with a PostgreSQL/Supabase database schema and WebSocket messaging for real-time notifications on under-18 freelance gigs, team matches, and forum posts.

---

## 🛠️ Prerequisites
1. [Node.js](https://nodejs.org/) installed (v16.0.0 or higher)
2. A free [Supabase Account](https://supabase.com/)

---

## 📦 Setting Up the Server Locally

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Configure Environment Variables**:
   Create a `.env` file in the `/backend` folder:
   ```env
   PORT=3000
   
   # Optional: Configure Supabase Database & File Storage Bucket
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_public_key
   SUPABASE_BUCKET_NAME=your_storage_bucket_for_receipts_screenshots
   ```

3. **Start the server**:
   ```bash
   # Run in development mode (with hot reloading)
   npm run dev

   # Run in production mode
   npm start
   ```

---

## 🗄️ Setting Up Supabase Database Tables

1. Log into your **Supabase Dashboard**.
2. Go to **SQL Editor** in your project sidebar.
3. Open a **New Query**.
4. Paste the complete contents of `database.sql` into the editor.
5. Hit **RUN**. This will instantly provision all necessary PostgreSQL tables with appropriate foreign keys and seed standard profiles!

---

## 📸 File Storage Setup for FamPay Screenshots

To store actual image receipt screenshots (e.g. uploaded via FamPay payment checks):
1. In **Supabase**, go to **Storage** tab.
2. Click **New Bucket**.
3. Name it exactly what you set for `SUPABASE_BUCKET_NAME` (e.g., `teenlancer-receipts`).
4. Set it to **Public** so that images can be viewed directly via links in Android.
