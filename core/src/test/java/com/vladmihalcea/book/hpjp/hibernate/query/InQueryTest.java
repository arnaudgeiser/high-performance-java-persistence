package com.vladmihalcea.book.hpjp.hibernate.query;

import com.vladmihalcea.book.hpjp.util.AbstractTest;
import java.util.stream.IntStream;
import org.junit.Test;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class InQueryTest extends AbstractTest {

  @Override
  protected Class<?>[] entities() {
    return new Class<?>[]{
        Post.class,
        PostComment.class
    };
  }

  @Override
  protected void additionalProperties(Properties properties) {
    properties.put("hibernate.jdbc.batch_size", "50");
    properties.put("hibernate.order_inserts", "true");
    properties.put("hibernate.query.in_clause_parameter_padding", "true");
  }

  @Test
  public void testPadding() {
    doInJPA(entityManager -> {
      List<Post> posts = IntStream.range(1, 16)
          .mapToObj(id -> {
            Post post = new Post();
            post.setId(id);
            post.setTitle(String.format("Post no. %d", id));
            return post;
          })
          .collect(Collectors.toList());

      List<PostComment> postComments = IntStream.range(0, posts.size())
          .mapToObj(i -> {
            int id = i + 1;
            PostComment postComment = new PostComment();
            postComment.setId(id);
            postComment.setComment(String.format("Post comment. %d", id));
            postComment.setPost(posts.get(i));
            return postComment;
          })
          .collect(Collectors.toList());

      posts.forEach(entityManager::persist);
      postComments.forEach(entityManager::persist);
    });

    doInJPA(entityManager -> {
      List<Post> postsSize3 = getPostByIds(entityManager, 1, 2, 3);
      List<PostComment> postsCommentsOfSize3 = getPostCommentsByPosts(entityManager, postsSize3);
      assertEquals(3, postsSize3.size());
      assertEquals(3, postsCommentsOfSize3.size());

      List<Post> postsSize4 = getPostByIds(entityManager, 1, 2, 3, 4);
      assertEquals(4, postsSize4.size());
      assertEquals(4, getPostCommentsByPosts(entityManager, postsSize4).size());

      List<Post> postsSize5 = getPostByIds(entityManager, 1, 2, 3, 4, 5);
      assertEquals(5, postsSize5.size());
      assertEquals(5, getPostCommentsByPosts(entityManager, postsSize5).size());

      List<Post> postsSize6 = getPostByIds(entityManager, 1, 2, 3, 4, 5, 6);
      assertEquals(6, postsSize6.size());
      assertEquals(6, getPostCommentsByPosts(entityManager, postsSize6).size());
    });
  }

  List<PostComment> getPostCommentsByPosts(EntityManager entityManager, List<Post> posts) {
    return entityManager.createQuery(
        "select pc " +
            "from PostComment pc " +
            "where pc.post in :posts", PostComment.class)
        .setParameter("posts", posts)
        .getResultList();
  }

  List<Post> getPostByIds(EntityManager entityManager, Integer... ids) {
    return entityManager.createQuery(
        "select p " +
            "from Post p " +
            "where p.id in :ids", Post.class)
        .setParameter("ids", Arrays.asList(ids))
        .getResultList();
  }

  @Entity(name = "Post")
  @Table(name = "post")
  public static class Post {

    @Id
    private Integer id;

    private String title;

    public Post() {
    }

    public Post(String title) {
      this.title = title;
    }

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  @Entity(name = "PostComment")
  @Table(name = "postcomment")
  public static class PostComment {

    @Id
    private Integer id;

    @ManyToOne
    private Post post;

    private String comment;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public Post getPost() {
      return post;
    }

    public void setPost(Post post) {
      this.post = post;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }
  }
}
