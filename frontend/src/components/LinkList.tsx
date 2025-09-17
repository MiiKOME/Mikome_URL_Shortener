import React, { useState, useEffect } from 'react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import {
  ExternalLink,
  Copy,
  Clock,
  MousePointer,
  RefreshCw,
  Calendar
} from 'lucide-react';
import { ShortenUrlResponse } from '../types/api';

interface LinkListProps {
  type: 'recent' | 'popular';
}

export const LinkList: React.FC<LinkListProps> = ({ type }) => {
  const [links, setLinks] = useState<ShortenUrlResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchLinks = async () => {
    setIsLoading(true);
    setError('');

    try {
      const endpoint = type === 'recent'
        ? 'http://localhost:8080/api/urls/recent?limit=10'
        : 'http://localhost:8080/api/urls/top-clicked?limit=10';

      const response = await fetch(endpoint);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: ShortenUrlResponse[] = await response.json();
      setLinks(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to fetch ${type} links`);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLinks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [type]);

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      // You could add a toast notification here
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  };

  const truncateUrl = (url: string, maxLength: number = 50): string => {
    if (url.length <= maxLength) return url;
    return url.substring(0, maxLength) + '...';
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);

    if (diffInHours < 1) {
      const diffInMinutes = Math.floor(diffInHours * 60);
      return `${diffInMinutes}m ago`;
    } else if (diffInHours < 24) {
      return `${Math.floor(diffInHours)}h ago`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      if (diffInDays === 1) return '1 day ago';
      return `${diffInDays} days ago`;
    }
  };

  const LinkItem: React.FC<{ link: ShortenUrlResponse }> = ({ link }) => (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-4">
        <div className="space-y-3">
          {/* Short URL and Actions */}
          <div className="flex items-center justify-between">
            <div className="flex-1 min-w-0">
              <p className="font-mono text-sm font-medium break-all">
                {link.shortUrl}
              </p>
            </div>
            <div className="flex items-center space-x-1 ml-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => copyToClipboard(link.shortUrl)}
                title="Copy short URL"
              >
                <Copy className="h-3 w-3" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => window.open(link.shortUrl, '_blank')}
                title="Open link"
              >
                <ExternalLink className="h-3 w-3" />
              </Button>
            </div>
          </div>

          {/* Original URL */}
          <div className="text-sm text-muted-foreground">
            <p className="break-all" title={link.originalUrl}>
              {truncateUrl(link.originalUrl, 60)}
            </p>
          </div>

          {/* Metadata */}
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-1">
                <MousePointer className="h-3 w-3" />
                <span>{link.clickCount} clicks</span>
              </div>
              <div className="flex items-center space-x-1">
                <Calendar className="h-3 w-3" />
                <span>{formatDate(link.createdAt)}</span>
              </div>
            </div>
            {link.expiresAt && (
              <div className="flex items-center space-x-1 text-orange-500">
                <Clock className="h-3 w-3" />
                <span>Expires {formatDate(link.expiresAt)}</span>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );

  if (isLoading) {
    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            Loading {type} links...
          </span>
          <RefreshCw className="h-4 w-4 animate-spin" />
        </div>
        {/* Loading skeleton */}
        {[1, 2, 3].map((i) => (
          <Card key={i}>
            <CardContent className="p-4">
              <div className="animate-pulse space-y-3">
                <div className="h-4 bg-muted rounded w-3/4"></div>
                <div className="h-3 bg-muted rounded w-full"></div>
                <div className="flex space-x-4">
                  <div className="h-3 bg-muted rounded w-16"></div>
                  <div className="h-3 bg-muted rounded w-20"></div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            {type === 'recent' ? 'Recent' : 'Popular'} Links
          </span>
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchLinks}
          >
            <RefreshCw className="h-4 w-4" />
          </Button>
        </div>
        <Card className="border-destructive">
          <CardContent className="p-4">
            <p className="text-destructive text-sm">{error}</p>
            <Button
              variant="outline"
              size="sm"
              onClick={fetchLinks}
              className="mt-2"
            >
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">
          {links.length} {type === 'recent' ? 'recent' : 'popular'} links
        </span>
        <Button
          variant="ghost"
          size="sm"
          onClick={fetchLinks}
          disabled={isLoading}
        >
          <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      {links.length === 0 ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-muted-foreground">
              No {type} links found
            </p>
            <p className="text-sm text-muted-foreground mt-1">
              Create your first short link above!
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {links.map((link, index) => (
            <LinkItem key={`${link.shortCode}-${index}`} link={link} />
          ))}
        </div>
      )}
    </div>
  );
};