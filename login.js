// script.js

let isLogin = true;

function toggleForm() {
  const formTitle = document.getElementById("form-title");
  const nameInput = document.getElementById("name");
  const submitBtn = document.getElementById("submit-btn");
  const toggleText = document.querySelector(".toggle");

  isLogin = !isLogin;

  if (isLogin) {
    formTitle.innerText = "Login";
    nameInput.style.display = "none";
    submitBtn.innerText = "Login";
    toggleText.innerText = "Don't have an account? Sign Up";
  } else {
    formTitle.innerText = "Sign Up";
    nameInput.style.display = "block";
    submitBtn.innerText = "Sign Up";
    toggleText.innerText = "Already have an account? Login";
  }
}

document.getElementById("auth-form").addEventListener("submit", async function (e) {
  e.preventDefault();

  const name = document.getElementById("name").value;
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  const endpoint = isLogin
    ? "http://localhost:5000/api/auth/login"
    : "http://localhost:5000/api/auth/register";

  const payload = isLogin ? { email, password } : { name, email, password };

  try {
    const res = await fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    const data = await res.json();

    if (res.ok) {
      alert(isLogin ? "Login successful!" : "Sign up successful!");
      console.log("Token/User:", data);
      // Optionally redirect:
      // window.location.href = "index.html";
    } else {
      alert(data.msg || "Something went wrong.");
    }
  } catch (error) {
    console.error(error);
    alert("Error connecting to server.");
  }
});

  