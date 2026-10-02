import { useState, useEffect, useCallback } from 'react';
import { reviewAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import RatingStars from './RatingStars';
import AverageRating from './AverageRating';
import ReviewCard from './ReviewCard';

/**
 * ReviewSection — Complete review system for a product detail page.
 * Loads reviews + summary from backend, handles submit + delete.
 *
 * @param {number} productId — The product to show reviews for
 */
const ReviewSection = ({ productId }) => {
  const { user } = useAuth();

  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState({ averageRating: 0, reviewCount: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Form state
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitMsg, setSubmitMsg] = useState('');

  // Check if current user already reviewed
  const hasReviewed = user && reviews.some((r) => r.userId === user.userId);

  // ── Load reviews + summary ──────────────────────────────────────────────
  const loadReviews = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [reviewRes, summaryRes] = await Promise.all([
        reviewAPI.getByProduct(productId),
        reviewAPI.getSummary(productId),
      ]);
      setReviews(reviewRes.data || []);
      setSummary(summaryRes.data || { averageRating: 0, reviewCount: 0 });
    } catch (err) {
      setError('Could not load reviews: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, [productId]);

  useEffect(() => {
    if (productId) loadReviews();
  }, [productId, loadReviews]);

  // ── Submit review ───────────────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      setSubmitMsg('Please select a star rating.');
      return;
    }
    setSubmitting(true);
    setSubmitMsg('');
    try {
      await reviewAPI.submit(productId, rating, comment.trim());
      setRating(0);
      setComment('');
      setSubmitMsg('✔ Review submitted!');
      loadReviews();
    } catch (err) {
      setSubmitMsg(err.message || 'Failed to submit review.');
    } finally {
      setSubmitting(false);
    }
  };

  // ── Delete review ───────────────────────────────────────────────────────
  const handleDelete = async (reviewId) => {
    if (!window.confirm('Delete your review?')) return;
    try {
      await reviewAPI.delete(reviewId);
      loadReviews();
    } catch (err) {
      alert('Could not delete review: ' + err.message);
    }
  };

  return (
    <div className="review-section">
      <h3>Reviews & Ratings</h3>

      {/* Average rating summary */}
      {loading ? (
        <p style={{ color: 'var(--txt-muted)', fontSize: '0.85rem' }}>Loading reviews…</p>
      ) : error ? (
        <p style={{ color: 'var(--error)', fontSize: '0.85rem' }}>{error}</p>
      ) : (
        <>
          <AverageRating
            average={summary.averageRating}
            count={summary.reviewCount}
          />

          {/* Write review form — only if logged in and hasn't reviewed yet */}
          {user && !hasReviewed && (
            <form className="review-form" onSubmit={handleSubmit}>
              <h4>Write a Review</h4>
              <RatingStars rating={rating} onRate={setRating} disabled={submitting} />
              <textarea
                placeholder="Share your experience with this product..."
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                maxLength={1000}
                disabled={submitting}
                aria-label="Review comment"
              />
              <button
                type="submit"
                className="submit-review-btn"
                disabled={submitting || rating === 0}
              >
                {submitting ? 'Submitting...' : 'Submit Review'}
              </button>
              {submitMsg && (
                <p style={{
                  marginTop: '8px',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  color: submitMsg.startsWith('✔') ? 'var(--success)' : 'var(--error)',
                }}>
                  {submitMsg}
                </p>
              )}
            </form>
          )}

          {/* Already reviewed notice */}
          {user && hasReviewed && (
            <p style={{
              fontSize: '0.82rem',
              color: 'var(--success)',
              fontWeight: 600,
              marginBottom: '16px',
            }}>
              ✔ You have already reviewed this product.
            </p>
          )}

          {/* Review list */}
          {reviews.length === 0 ? (
            <div className="no-reviews">
              No reviews yet. Be the first to review!
            </div>
          ) : (
            <div className="review-list">
              {reviews.map((review) => (
                <ReviewCard
                  key={review.id}
                  review={review}
                  currentUserId={user?.userId}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default ReviewSection;
