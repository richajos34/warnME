// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyCPVduCdCW5jP_UUg4qmV-_2g7tVtFKa1c",
  authDomain: "incidentreporting-7ecf7.firebaseapp.com",
  projectId: "incidentreporting-7ecf7",
  storageBucket: "incidentreporting-7ecf7.appspot.com",
  messagingSenderId: "244443380986",
  appId: "1:244443380986:web:1f1e18bece7bf79bf41d43",
  measurementId: "G-KQ4SDG0DQG"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);