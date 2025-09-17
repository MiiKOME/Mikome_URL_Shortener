# URL Shortener Frontend

A modern React frontend for the URL Shortener service built with TypeScript, Tailwind CSS, and shadcn/ui components.

## Features

- 🔗 Create short links from long URLs
- 📊 View real-time statistics dashboard
- 📋 Browse recent and popular links
- ⏰ Set expiration dates for links
- 📱 Responsive design with mobile support
- 🌙 Dark mode support (via shadcn/ui)
- 📋 One-click copy functionality
- 🔄 Auto-refresh data

## Tech Stack

- **React 19** with TypeScript
- **Tailwind CSS** for styling
- **shadcn/ui** for beautiful components
- **Lucide React** for icons
- **Class Variance Authority** for component variants

## Getting Started

### Prerequisites

- Node.js 16+ and npm
- URL Shortener backend running on `http://localhost:8080`

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

### Environment Variables

Create a `.env` file in the frontend directory:

```env
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_APP_NAME=URL Shortener
REACT_APP_VERSION=1.0.0
```

## Project Structure

```
src/
├── components/           # React components
│   ├── ui/              # shadcn/ui base components
│   ├── UrlShortener.tsx # URL shortening form
│   ├── StatsDashboard.tsx # Statistics display
│   └── LinkList.tsx     # Recent/popular links
├── lib/                 # Utilities and API client
│   ├── utils.ts         # Utility functions
│   └── api.ts          # API client
├── types/              # TypeScript type definitions
│   └── api.ts          # API types
└── App.tsx             # Main application component
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm run eject` - Eject from Create React App

## API Integration

The frontend integrates with the following backend endpoints:

- `POST /api/urls/shorten` - Create short URL
- `GET /api/urls/recent` - Get recent URLs
- `GET /api/urls/top-clicked` - Get popular URLs
- `GET /api/urls/statistics` - Get system statistics
- `GET /api/urls/{shortCode}/info` - Get URL info
- `DELETE /api/urls/cleanup` - Clean expired URLs

## Contributing

1. Follow the existing code style
2. Use TypeScript for all new files
3. Add proper error handling
4. Test your changes thoroughly

## Notes

- The app expects the backend to be running on port 8080
- CORS is handled by the backend `@CrossOrigin` annotation
- All API calls include proper error handling
- The UI is fully responsive and works on mobile devices