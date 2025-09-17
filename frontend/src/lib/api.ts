import {
  ShortenUrlRequest,
  ShortenUrlResponse,
  SystemStatistics,
  ApiError
} from '../types/api';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = API_BASE_URL) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;

    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const error: ApiError = {
          message: `HTTP error! status: ${response.status}`,
          status: response.status,
        };

        // Try to get error message from response body
        try {
          const errorData = await response.json();
          if (errorData.message) {
            error.message = errorData.message;
          }
        } catch {
          // If parsing error response fails, use default message
        }

        throw error;
      }

      // Handle empty responses (like DELETE requests)
      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return '' as unknown as T;
      }

      return await response.json();
    } catch (error) {
      if (error instanceof Error) {
        // Network or parsing error
        const apiError: ApiError = {
          message: error.message,
        };
        throw apiError;
      }
      throw error;
    }
  }

  // URL Shortening APIs
  async shortenUrl(request: ShortenUrlRequest): Promise<ShortenUrlResponse> {
    return this.request<ShortenUrlResponse>('/api/urls/shorten', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async getUrlInfo(shortCode: string): Promise<ShortenUrlResponse> {
    return this.request<ShortenUrlResponse>(`/api/urls/${shortCode}/info`);
  }

  async getRecentUrls(limit: number = 10): Promise<ShortenUrlResponse[]> {
    return this.request<ShortenUrlResponse[]>(`/api/urls/recent?limit=${limit}`);
  }

  async getTopClickedUrls(limit: number = 10): Promise<ShortenUrlResponse[]> {
    return this.request<ShortenUrlResponse[]>(`/api/urls/top-clicked?limit=${limit}`);
  }

  async getStatistics(): Promise<SystemStatistics> {
    return this.request<SystemStatistics>('/api/urls/statistics');
  }

  async cleanupExpiredUrls(): Promise<string> {
    return this.request<string>('/api/urls/cleanup', {
      method: 'DELETE',
    });
  }

  // Utility method to validate URLs
  isValidUrl(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }

  // Method to get the full short URL
  getShortUrl(shortCode: string): string {
    return `${this.baseUrl}/${shortCode}`;
  }
}

// Create and export a singleton instance
export const apiClient = new ApiClient();

// Export the class for testing or creating custom instances
export { ApiClient };

// Custom hooks for API calls (optional, can be used in components)
export const useApiError = () => {
  const handleError = (error: unknown): string => {
    if (error && typeof error === 'object' && 'message' in error) {
      return (error as ApiError).message;
    }
    return 'An unexpected error occurred';
  };

  return { handleError };
};