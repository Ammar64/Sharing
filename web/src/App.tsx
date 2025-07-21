import { Route, Routes } from 'react-router';
import './App.css';
import Home from './pages/Home/Home';
import NotFound from './pages/NotFound/NotFound';

export function App() {

  return (
    <Routes>
      <Route path="/" element={<Home/>}/>
      <Route path="*" element={<NotFound/>}/>
    </Routes>
  );
}
