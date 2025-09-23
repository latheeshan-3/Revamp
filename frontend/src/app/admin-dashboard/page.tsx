"use client";

import { useEffect, useState } from "react";
import { getGreeting } from "../../utils/greeting";
import { decodeToken, TokenPayload } from "../../utils/jwt";
import Link from "next/link";

export default function AdminDashboard() {
  const [user, setUser] = useState<TokenPayload | null>(null);

  // Add Employee Form State
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    role: "EMPLOYEE",
  });
  const [message, setMessage] = useState("");

 // const GATEWAY_URL = "http://localhost:4000";
 const GATEWAY_URL = process.env.NEXT_PUBLIC_GATEWAY_URL as string;


  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      const decoded = decodeToken(token);
      setUser(decoded);
    }
  }, []);

  const greeting = getGreeting();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");
    try {
      const res = await fetch(`${GATEWAY_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Failed to register employee");
      setMessage(" Employee created successfully!");
      setForm({ username: "", email: "", password: "", role: "EMPLOYEE" });
    } catch (err: any) {
      setMessage(` ${err.message}`);
    }
  };

  
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

      <div className="bg-white shadow-lg rounded-2xl p-8 w-full max-w-lg text-center">
        {/* Greeting Section */}
        <h1 className="text-2xl font-bold mb-4">
          {greeting}, {user?.username || "Admin"}!
        </h1>
        <p className="text-gray-700 mb-2">Role: {user?.role || "ADMIN"}</p>
        <p className="text-gray-700 mb-6">
          Email: {user?.email || "admin@revamp.com"}
        </p>

        {/* Add Employee Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-left">
          <h2 className="text-lg font-bold text-center">Add Employee</h2>
          <input
            type="text"
            name="username"
            value={form.username}
            onChange={handleChange}
            placeholder="Employee Name"
            required
            className="w-full px-4 py-2 border rounded-lg"
          />
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            placeholder="Employee Email"
            required
            className="w-full px-4 py-2 border rounded-lg"
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Password"
            required
            className="w-full px-4 py-2 border rounded-lg"
          />
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
          >
            Add Employee
          </button>
          {message && (
            <p
              className={`mt-2 text-sm ${
                message.startsWith("✅") ? "text-green-600" : "text-red-600"
              }`}
            >
              {message}
            </p>
          )}
        </form>

                 {/* for  Music */}
     
          <div className="mt-6">
          <button
            onClick={toggleMusic}
            className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 transition">
            {isPlaying ? "⏸ Pause Music" : "click ME"}
          </button>
          <audio id="bg-audio" src="/music/theme3.mp3" loop  />
        </div>
                          {/* for  Music  */}


      </div>
    </main>
  );
}
