import { Outlet, Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import Header from "./Header";
import Footer from "./Footer";
import { Container } from "./Container";
import { ROUTES } from "@/config/routes";

export default function MainLayout() {
  const { isAuthenticated, isLoading } = useAuth();

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.auth.login} replace />;
  }

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 py-6">
        <Container>
          <Outlet />
        </Container>
      </main>
      <Footer />
    </div>
  );
}

