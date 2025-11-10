import { ThemeToggle } from "@/components/common/ThemeToggle";
function App() {

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-8">
      <div className="fixed top-4 right-4">
        <ThemeToggle />
      </div>
      <div className="flex flex-col items-center justify-center">
        <h1 className="text-4xl font-bold mb-4">Welcome to Home Page</h1>
        <h2>Use the theme toggle in the top right corner to change the theme</h2>
      </div>
    </div>
  );
}

export default App
