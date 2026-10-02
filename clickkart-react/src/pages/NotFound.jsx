import { Link } from 'react-router-dom';

const NotFound = () => (
  <div className="not-found-page" role="main">
    <h1>404</h1>
    <p>Oops! The page you're looking for doesn't exist.</p>
    <Link to="/" aria-label="Go back to home page">← Back to Home</Link>
  </div>
);

export default NotFound;
