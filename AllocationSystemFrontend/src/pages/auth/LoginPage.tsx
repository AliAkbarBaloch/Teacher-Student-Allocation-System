// components
import { LoginForm } from "@/features/auth/components/LoginForm";
// icons
import { GraduationCap } from "lucide-react";
// translations
import { useTranslation } from "react-i18next";
import { useState } from "react";

export default function LoginPage() {
  const { t } = useTranslation();
  const [imageError, setImageError] = useState(false);
  
  return (
    <div className="min-h-screen max-h-screen flex max-w-[1920px] mx-auto overflow-hidden justify-center">
      {/* Left Column - Login Form */}
      <div className="flex-1 lg:flex-[0_0_40%] flex flex-col bg-background overflow-y-auto">
        <div className="flex-1 flex flex-col px-4 sm:px-6 lg:px-12 xl:px-16 2xl:px-20">
          <div className="mx-auto w-full max-w-lg xl:max-w-xl 2xl:max-w-2xl flex-1 flex flex-col">
            {/* Logo and Branding */}
            <div className="w-full max-w-md flex items-center gap-2 mt-8 mb-12">
              <div className="h-10 w-10 rounded-lg flex items-center justify-center bg-primary">
                <GraduationCap className="h-6 w-6 text-primary-foreground" />
              </div>
              <span className="text-2xl font-bold">{t("common:app.title")}</span>
            </div>

            <div className="flex-1 flex flex-col justify-center">
              {/* Welcome Section */}
              <div className="w-full max-w-md mb-8">
                <h1 className="text-3xl font-bold tracking-tight mb-2">
                  {t("auth:login.title")}
                </h1>
                <p className="text-muted-foreground">
                  {t("auth:login.subtitle")}
                </p>
              </div>

              {/* Login Form */}
              <LoginForm />

              {/* Footer */}
              <div className="mt-12 text-center text-sm text-muted-foreground md:mr-12">
                <p>{t("common:footer.copyright")} {new Date().getFullYear()} {t("common:footer.university")}</p>
                <a
                  href="https://www.uni-passau.de/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:underline mt-1 inline-block text-primary hover:text-primary/80"
                >
                  {t("common:footer.privacyPolicy")}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Column - Dashboard Preview */}
      <div className="hidden lg:flex lg:flex-[0_0_40%] relative overflow-hidden rounded-lg m-8 max-h-[calc(100vh-4rem)] shrink-0">
        {/* Background Image */}
        <div
          className="absolute inset-0 bg-cover bg-center bg-no-repeat"
          style={{
            backgroundImage: `url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1920&h=1080&fit=crop&q=80')`,
          }}
        ></div>

        {/* Gradient Overlay - University of Passau Colors */}
        <div
          className="absolute inset-0 z-10 opacity-80"
          style={{
            background:
              "linear-gradient(135deg, #F8971C 0%, rgba(216, 122, 0, 0.9) 50%, #A6ADAC 100%)",
          }}
        ></div>

        {/* Background Pattern Overlay */}
        <div className="absolute inset-0 z-20 opacity-10">
          <div
            className="absolute inset-0"
            style={{
              backgroundImage: `radial-gradient(circle at 2px 2px, white 1px, transparent 3%)`,
              backgroundSize: "40px 40px",
            }}
          />
        </div>

        {/* Content Container */}
        <div className="relative z-30 flex flex-col justify-center items-center px-8 xl:px-16 text-white w-full h-full py-12">
          {/* Text Content */}
          <div className="absolute top-12 xl:top-16 left-8 xl:left-16 right-8 xl:right-16">
            <h2 className="text-3xl xl:text-4xl 2xl:text-5xl font-bold mb-4 leading-tight">
              {t("common:app.tagline")}
            </h2>
            <p className="text-base xl:text-lg 2xl:text-xl opacity-90 max-w-2xl">
              {t("common:app.description")}
            </p>
          </div>

          {/* Dashboard Preview Image Container */}
          <div className="relative w-full max-w-5xl xl:max-w-6xl mt-24 xl:mt-32">
            {/* Shadow/Glow Effect */}
            <div className="absolute -inset-6 bg-white/10 rounded-2xl blur-3xl"></div>

            {/* Dashboard Preview Image */}
            <div className="relative rounded-2xl overflow-hidden shadow-2xl border-4 border-white/30 bg-white">
              {imageError ? (
                <div 
                  className="w-full h-full flex flex-col items-center justify-center p-16"
                  style={{
                    background: "linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%)",
                  }}
                >
                  <svg 
                    width="120" 
                    height="120" 
                    viewBox="0 0 24 24" 
                    fill="none" 
                    stroke="currentColor" 
                    strokeWidth="1.5" 
                    className="text-gray-500 mb-4"
                  >
                    <rect x="3" y="3" width="7" height="7" rx="1" />
                    <rect x="14" y="3" width="7" height="7" rx="1" />
                    <rect x="3" y="14" width="7" height="7" rx="1" />
                    <rect x="14" y="14" width="7" height="7" rx="1" />
                  </svg>
                  <p className="text-xl font-semibold text-gray-600 mb-2">Dashboard Preview</p>
                  <p className="text-sm text-gray-500">Internship Allocation System</p>
                </div>
              ) : (
                // TODO: Replace with the actual dashboard preview image
                <img
                  src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1920&h=1080&fit=crop&q=80"
                  alt="Dashboard Preview"
                  className="w-full h-auto object-cover"
                  onError={() => setImageError(true)}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

