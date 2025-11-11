import { Outlet } from "react-router-dom";
import Header from "@/components/layout/Header";

import Footer from "@/components/layout/Footer";

export default function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <div className="flex flex-1">
        <main className="flex-1 p-4">
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  );
}
