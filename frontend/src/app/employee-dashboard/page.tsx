"use client";

import { useEffect, useState } from "react";
import { getGreeting } from "../../utils/greeting";
import { decodeToken, TokenPayload } from "../../utils/jwt";
import Link from "next/link";

export default function EmployeeDashboard() {
  const [user, setUser] = useState<TokenPayload | null>(null);

  


  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      const decoded = decodeToken(token);
      setUser(decoded);
    }

  }, []);

  const greeting = getGreeting();


// for music 
 const [isPlaying, setIsPlaying] = useState(false); 
 const toggleMusic = () => {
    const audio = document.getElementById("bg-audio") as HTMLAudioElement;
    if (!audio) return;

    if (audio.paused) {
      audio.play();
      setIsPlaying(true);
    } else {
      audio.pause();
      setIsPlaying(false);
    }
  };
// for music 



  return (
    
    <main className="min-h-screen flex flex-col items-center justify-center bg-yellow-50 px-4 relative">
      
      {/* Back to Home button in top-right */}
      <div className="absolute top-4 right-4">
        <Link href="/">
          <button className="px-3 py-2 text-sm rounded-lg bg-gray-800 text-white hover:bg-gray-900 transition">
            Back to Home
          </button>
        </Link>
      </div>

      <div className="bg-white shadow-lg rounded-2xl p-8 w-full max-w-md text-center">
        <h1 className="text-2xl font-bold mb-4">{greeting}, {user?.username || "Employee"}!</h1>
        <p className="text-gray-700 mb-2">Role: {user?.role || "EMPLOYEE"}</p>
        <p className="text-gray-700">Email: {user?.email || "employee@revamp.com"}</p>

                             {/* for  Music */}
    
          <div className="mt-6">
          <button
            onClick={toggleMusic}
            className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 transition">
            {isPlaying ? "⏸ Pause Music" : "click ME"}
          </button>
          <audio id="bg-audio" src="/music/theme2.mp3" loop  />
        </div>
                          {/* for  Music  */}

      </div>
    </main>
  );
}

