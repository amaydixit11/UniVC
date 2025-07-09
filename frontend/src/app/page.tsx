"use client";

import { useState } from "react";
import HealthCheck from "@/components/HealthCheck";
import { Shield, FileText, CheckCircle, Settings } from "lucide-react";
import FileUploadComponent from "@/components/FileUpload";

export default function MainPage() {
  const [activeTab, setActiveTab] = useState<"upload" | "health" | "about">(
    "upload"
  );

  const tabs = [
    { id: "upload", label: "Upload & Verify", icon: FileText },
    { id: "health", label: "System Status", icon: CheckCircle },
    { id: "about", label: "About", icon: Settings },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case "upload":
        return <FileUploadComponent />;
      case "health":
        return (
          <div className="max-w-4xl mx-auto p-6">
            <HealthCheck />
          </div>
        );
      case "about":
        return (
          <div className="max-w-4xl mx-auto p-6">
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-2xl font-bold mb-4">
                About Unified VC Verifier
              </h2>
              <div className="space-y-4 text-gray-700">
                <p>
                  The Unified Verifiable Credential Verification Platform
                  provides a single, comprehensive solution for verifying
                  multiple credential formats including:
                </p>
                <ul className="list-disc pl-6 space-y-2">
                  <li>
                    <strong>SD-JWT:</strong> Selective Disclosure JSON Web
                    Tokens
                  </li>
                  <li>
                    <strong>W3C VC 1.1/2.0:</strong> W3C Verifiable Credentials
                  </li>
                  <li>
                    <strong>ISO mDL:</strong> ISO Mobile Driving License
                  </li>
                  <li>
                    <strong>Generic JSON:</strong> Standard JSON credentials
                  </li>
                </ul>
                <p>
                  This platform eliminates the complexity of handling multiple
                  verification libraries and provides a unified interface for
                  credential verification.
                </p>
                <div className="bg-blue-50 p-4 rounded-lg">
                  <h3 className="font-semibold text-blue-900 mb-2">
                    Key Features:
                  </h3>
                  <ul className="text-blue-800 space-y-1">
                    <li>• Automatic format detection</li>
                    <li>• Comprehensive structure validation</li>
                    <li>• Detailed verification reports</li>
                    <li>• Support for multiple credential standards</li>
                    <li>• Developer-friendly API</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        );
      default:
        return <FileUploadComponent />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-3">
              <Shield className="w-8 h-8 text-blue-600" />
              <div>
                <h1 className="text-xl font-bold text-gray-900">
                  Unified VC Verifier
                </h1>
                <p className="text-sm text-gray-500">
                  Multi-format credential verification platform
                </p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500">v1.0.0</span>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation Tabs */}
      <nav className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-8">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`flex items-center space-x-2 py-4 px-1 border-b-2 font-medium text-sm ${
                    activeTab === tab.id
                      ? "border-blue-500 text-blue-600"
                      : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="py-8">{renderContent()}</main>

      {/* Footer */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-2 mb-4 md:mb-0">
              <Shield className="w-5 h-5 text-blue-600" />
              <span className="text-gray-600">Unified VC Verifier</span>
            </div>
            <div className="text-sm text-gray-500">
              Built for secure and reliable credential verification
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
