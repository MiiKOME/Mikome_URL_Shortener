import React, { useState } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent } from './ui/card';
import { Copy, ExternalLink, Clock } from 'lucide-react';
import { ShortenUrlRequest, ShortenUrlResponse } from '../types/api';

export const UrlShortener: React.FC = () => {
  const [url, setUrl] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<ShortenUrlResponse | null>(null);
  const [error, setError] = useState('');

  const validateUrl = (url: string): boolean => {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!url.trim()) {
      setError('Please enter a URL');
      return;
    }

    if (!validateUrl(url)) {
      setError('Please enter a valid URL');
      return;
    }

    setIsLoading(true);
    setError('');
    setResult(null);

    try {
      const request: ShortenUrlRequest = {
        url: url.trim(),
        ...(expiresAt && { expiresAt })
      };

      const response = await fetch('http://localhost:8080/api/urls/shorten', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: ShortenUrlResponse = await response.json();
      setResult(data);
      setUrl('');
      setExpiresAt('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to shorten URL');
    } finally {
      setIsLoading(false);
    }
  };

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      // You could add a toast notification here
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  };

  return (
    <div className="space-y-6">
      {/* URL Shortening Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <Input
            type="url"
            placeholder="Enter your URL here (e.g., https://example.com)"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            className="w-full"
            disabled={isLoading}
          />
        </div>

        {/* Optional Expiry Date */}
        <div className="flex items-center space-x-2">
          <Clock className="h-4 w-4 text-muted-foreground" />
          <Input
            type="datetime-local"
            placeholder="Expiry date (optional)"
            value={expiresAt}
            onChange={(e) => setExpiresAt(e.target.value)}
            className="w-full"
            disabled={isLoading}
          />
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={isLoading || !url.trim()}
        >
          {isLoading ? 'Shortening...' : 'Shorten URL'}
        </Button>
      </form>

      {/* Error Display */}
      {error && (
        <Card className="border-destructive">
          <CardContent className="pt-6">
            <p className="text-destructive text-sm">{error}</p>
          </CardContent>
        </Card>
      )}

      {/* Result Display */}
      {result && (
        <Card className="border-green-200 bg-green-50 dark:border-green-800 dark:bg-green-950">
          <CardContent className="pt-6">
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground">
                Short URL created successfully!
              </div>

              {/* Short URL Display */}
              <div className="flex items-center space-x-2 p-3 bg-background rounded-md border">
                <span className="font-mono text-sm flex-1 break-all">
                  {result.shortUrl}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => copyToClipboard(result.shortUrl)}
                >
                  <Copy className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => window.open(result.shortUrl, '_blank')}
                >
                  <ExternalLink className="h-4 w-4" />
                </Button>
              </div>

              {/* Original URL */}
              <div className="text-sm">
                <span className="text-muted-foreground">Original: </span>
                <span className="break-all">{result.originalUrl}</span>
              </div>

              {/* Metadata */}
              <div className="grid grid-cols-2 gap-4 text-sm text-muted-foreground">
                <div>
                  <span className="font-medium">Created: </span>
                  {new Date(result.createdAt).toLocaleString()}
                </div>
                {result.expiresAt && (
                  <div>
                    <span className="font-medium">Expires: </span>
                    {new Date(result.expiresAt).toLocaleString()}
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};