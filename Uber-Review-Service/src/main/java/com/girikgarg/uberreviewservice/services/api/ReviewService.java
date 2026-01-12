public interface ReviewService {
    public Optional <Review> findReviewById(Long id);

    public List <Review> findAllReviews();

    public boolean deleteReviewById(Long id);
}  
