import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './components/ui/card';
import { UrlShortener } from './components/UrlShortener';
import { StatsDashboard } from './components/StatsDashboard';
import { LinkList } from './components/LinkList';

function App() {
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto py-8 px-4">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-foreground mb-2">
            🔗 URL Shortener
          </h1>
          <p className="text-muted-foreground">
            Transform long URLs into short, shareable links
          </p>
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - URL Shortener */}
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Create Short Link</CardTitle>
              </CardHeader>
              <CardContent>
                <UrlShortener />
              </CardContent>
            </Card>

            {/* Recent Links */}
            <Card>
              <CardHeader>
                <CardTitle>Recent Links</CardTitle>
              </CardHeader>
              <CardContent>
                <LinkList type="recent" />
              </CardContent>
            </Card>

            {/* Popular Links */}
            <Card>
              <CardHeader>
                <CardTitle>Popular Links</CardTitle>
              </CardHeader>
              <CardContent>
                <LinkList type="popular" />
              </CardContent>
            </Card>
          </div>

          {/* Right Column - Stats Dashboard */}
          <div className="lg:col-span-1">
            <Card className="sticky top-8">
              <CardHeader>
                <CardTitle>Statistics</CardTitle>
              </CardHeader>
              <CardContent>
                <StatsDashboard />
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
