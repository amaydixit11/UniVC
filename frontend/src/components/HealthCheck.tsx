"use client";

import { useState, useEffect } from "react";
import { apiClient } from "@/lib/api";
import { ApiResponse, HealthData } from "@/types/api";
import { CheckCircle, XCircle, Loader2 } from "lucide-react";

export default function HealthCheck() {
  const [health, setHealth] = useState<HealthData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const checkHealth = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await apiClient.get<ApiResponse<HealthData>>("/health");
      setHealth(response.data.data);
    } catch (err: any) {
      setError(err.message || "Failed to check health");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkHealth();
  }, []);

  const getStatusIcon = () => {
    if (loading) return <Loader2 className="w-5 h-5 animate-spin" />;
    if (error) return <XCircle className="w-5 h-5 text-red-500" />;
    if (health?.status === "UP")
      return <CheckCircle className="w-5 h-5 text-green-500" />;
    return <XCircle className="w-5 h-5 text-red-500" />;
  };

  const getStatusText = () => {
    if (loading) return "Checking...";
    if (error) return "Backend Offline";
    if (health?.status === "UP") return "Backend Online";
    return "Backend Error";
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <h2 className="text-xl font-semibold mb-4">System Status</h2>

      <div className="flex items-center space-x-3 mb-4">
        {getStatusIcon()}
        <span className="font-medium">{getStatusText()}</span>
      </div>

      {health && (
        <div className="space-y-2 text-sm text-gray-600">
          <div>Application: {health.application}</div>
          <div>Version: {health.version}</div>
          <div>Last Check: {new Date(health.timestamp).toLocaleString()}</div>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded p-3 text-red-700 text-sm">
          Error: {error}
        </div>
      )}

      <button
        onClick={checkHealth}
        disabled={loading}
        className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
      >
        {loading ? "Checking..." : "Refresh"}
      </button>
    </div>
  );
}
