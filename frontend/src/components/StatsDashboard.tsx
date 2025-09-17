import React, { useState, useEffect } from 'react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import {
  BarChart3,
  Link,
  MousePointer,
  Calendar,
  RefreshCw,
  TrendingUp
} from 'lucide-react';
import { SystemStatistics } from '../types/api';

export const StatsDashboard: React.FC = () => {
  const [stats, setStats] = useState<SystemStatistics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchStats = async () => {
    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/urls/statistics');

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: SystemStatistics = await response.json();
      setStats(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch statistics');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const formatNumber = (num: number): string => {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  };

  const StatCard: React.FC<{
    title: string;
    value: number;
    icon: React.ReactNode;
    color: string;
  }> = ({ title, value, icon, color }) => (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-center space-x-3">
          <div className={`p-2 rounded-lg ${color}`}>
            {icon}
          </div>
          <div className="flex-1">
            <p className="text-sm text-muted-foreground">{title}</p>
            <p className="text-2xl font-bold">{formatNumber(value)}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-medium">Loading statistics...</h3>
          <RefreshCw className="h-4 w-4 animate-spin" />
        </div>
        {/* Loading skeleton */}
        {[1, 2, 3, 4].map((i) => (
          <Card key={i}>
            <CardContent className="p-4">
              <div className="animate-pulse">
                <div className="h-4 bg-muted rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-muted rounded w-1/2"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-medium">Statistics</h3>
          <Button
            variant="ghost"
            size="sm"
            onClick={fetchStats}
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
              onClick={fetchStats}
              className="mt-2"
            >
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-medium">Overview</h3>
        <Button
          variant="ghost"
          size="sm"
          onClick={fetchStats}
          disabled={isLoading}
        >
          <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      <div className="space-y-3">
        <StatCard
          title="Total URLs"
          value={stats.totalUrls}
          icon={<Link className="h-4 w-4 text-white" />}
          color="bg-blue-500"
        />

        <StatCard
          title="Total Clicks"
          value={stats.totalClicks}
          icon={<MousePointer className="h-4 w-4 text-white" />}
          color="bg-green-500"
        />

        <StatCard
          title="Avg. Clicks"
          value={Math.round(stats.averageClicks)}
          icon={<TrendingUp className="h-4 w-4 text-white" />}
          color="bg-purple-500"
        />

        <StatCard
          title="Today's URLs"
          value={stats.urlsToday}
          icon={<Calendar className="h-4 w-4 text-white" />}
          color="bg-orange-500"
        />
      </div>

      {/* Quick Actions */}
      <Card>
        <CardContent className="p-4">
          <h4 className="font-medium mb-3">Quick Actions</h4>
          <div className="space-y-2">
            <Button
              variant="outline"
              size="sm"
              className="w-full justify-start"
              onClick={() => {
                // This could trigger a cleanup operation
                fetch('http://localhost:8080/api/urls/cleanup', {
                  method: 'DELETE'
                }).then(() => {
                  fetchStats(); // Refresh stats after cleanup
                });
              }}
            >
              <BarChart3 className="h-4 w-4 mr-2" />
              Clean Expired URLs
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="text-xs text-muted-foreground text-center">
        Last updated: {new Date().toLocaleTimeString()}
      </div>
    </div>
  );
};