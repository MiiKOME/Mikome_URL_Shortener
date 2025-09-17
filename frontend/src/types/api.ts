// API Request Types
export interface ShortenUrlRequest {
  url: string;
  expiresAt?: string; // ISO date string
}

// API Response Types
export interface ShortenUrlResponse {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  createdAt: string; // ISO date string
  expiresAt?: string; // ISO date string
  clickCount: number;
}

export interface SystemStatistics {
  totalUrls: number;
  totalClicks: number;
  averageClicks: number;
  urlsToday: number;
}

// Component Props Types
export interface UrlMapping {
  id: number;
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  createdAt: string;
  expiresAt?: string;
  clickCount: number;
}

// API Client Types
export interface ApiError {
  message: string;
  status?: number;
}