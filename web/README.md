# EcoMap Clinical - Web Deployment Guide (Single Session Mode)

This folder contains the complete, standalone **Single-Session Web Version of EcoMap Clinical**.

## Features & Privacy Architecture
- **Single-Session (Zero Storage)**: No client records, images, or notes are stored on the server or in persistent browser storage (`localStorage`).
- **Memory-Only State**: All nodes, drag coordinates, and notes reside solely in browser RAM during the active session. Closing or refreshing the browser tab completely wipes all data.
- **Client PDF Generation**: Clean clinical report generation runs entirely in the user's browser using native print/PDF rendering.
- **Zero Dependencies**: Standalone HTML, CSS, and HTML5 Canvas JS—requires no server backend, no database, no Node.js runtime, and no API keys.

---

## 🚀 How to Deploy on Your Website

### Option 1: Static Hosting (GitHub Pages, Vercel, Netlify, Cloudflare Pages)
1. Copy `index.html` to your web server root or repository.
2. Publish to your hosting provider.
3. Access at `https://yourwebsite.com/index.html`.

### Option 2: Embed on WordPress / Squarespace / Wix via iFrame
Add an HTML block on your website page:
```html
<iframe 
  src="https://yourwebsite.com/ecomap/index.html" 
  width="100%" 
  height="750px" 
  style="border: 1px solid #E2E8F0; border-radius: 8px;"
  title="EcoMap Clinical Tool">
</iframe>
```

---

## 🔒 Security & HIPAA Compliance Notice
Because this web edition operates strictly in **Single Session (Memory-Only) Mode**:
- No PHI (Protected Health Information) is saved to local disks or cloud databases.
- When clinicians complete an assessment, they export/print the PDF to their secure EMR and close the tab.
