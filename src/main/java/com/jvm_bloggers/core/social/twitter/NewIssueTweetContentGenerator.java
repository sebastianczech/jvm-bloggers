package com.jvm_bloggers.core.social.twitter;

import com.jvm_bloggers.core.blogpost_redirect.LinkGenerator;
import com.jvm_bloggers.entities.blog.Blog;
import com.jvm_bloggers.entities.blog_post.BlogPost;
import com.jvm_bloggers.entities.newsletter_issue.NewsletterIssue;

import io.vavr.API;
import io.vavr.collection.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.Objects;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PACKAGE;

@Component
@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
class NewIssueTweetContentGenerator {


    private List<String> NEW_ISSUE_TWEET_PREFIXES = List.of(
        "Nowy numer #<number> jest już online - <link> z postami m.in. ",
        "Nowe wydanie #<number> jest już online - <link> z wpisami m.in. ",
        "Możecie już przeczytać nowy numer JVM Bloggers #<number> - <link> z postami m.in. ",
        "Jest piątek, jest nowy JVM Bloggers #<number> :) - <link> z postami m.in. ",
        "Piątkowa porcja wpisów z polskiego świata JVM numer <number> jest juś online :) - <link> z postami m.in. "
    );

    private static final int TWEET_MAX_LENGTH = 280;
    private static final String MESSAGE_TEMPLATE =
        "Nowy numer #<number> już online - <link> z postami m.in. <personal1>"
            + "<if(company && personal2)>, <company> i <personal2>"
            + "<elseif(company)> i <company>"
            + "<elseif(personal2)> i <personal2><endif> #java #jvm";

    private final LinkGenerator linkGenerator;

    public String generateTweetContent(NewsletterIssue issue) {

        String completeTemplate = NEW_ISSUE_TWEET_PREFIXES.shuffle().get(0)
            + generateTwitterHandles(getPersonalBlogs(issue))

            + "TODO";

        final List<String> personals =

                .map(Blog::getTwitter)
                .filter(Objects::nonNull)
                .shuffle()
                .take(2)
                .padTo(2, null);

        final String company =
            List.ofAll(issue.getBlogPosts())
                .map(BlogPost::getBlog)
                .filter(Blog::isCompany)
                .map(Blog::getTwitter)
                .filter(Objects::nonNull)
                .shuffle()
                .getOrElse((String) null);

        final String issueLink = linkGenerator.generateIssueLink(issue.getIssueNumber());

        final ST template = new ST(MESSAGE_TEMPLATE);
        template.add("number", issue.getIssueNumber());
        template.add("link", issueLink);
        template.add("personal1", personals.head());
        template.add("personal2", personals.last());
        template.add("company", company);
        final String tweetContent = template.render();

    }

    private List<Blog> getPersonalBlogs(NewsletterIssue issue) {
        return List.ofAll(issue.getBlogPosts())
            .map(BlogPost::getBlog)
            .filter(Blog::isPersonal);
    }

    private List<Blog> getCompanyBlogs(NewsletterIssue issue) {
        return List.ofAll(issue.getBlogPosts())
            .map(BlogPost::getBlog)
            .filter(Blog::isCompany);
    }

    private String generateTwitterHandles(List<Blog> blogs, int maxNumberOfHandles) {
        List<String> twitterHandles = blogs
            .map(Blog::getTwitter)
            .filter(Objects::nonNull)
            .shuffle()
            .take()

        String handles = twitterHandles.collect(Collectors.joining(", "));
        API.Match(twitterHandles.single()).of(
            Case($Success($()), SUCCESS),
            Case($Failure($()), ERROR)
        )
        )

    }

    private boolean tweetIsTooLong(String tweetContent, int originalIssuesLinkLength) {
        return (tweetContent.length() - originalIssuesLinkLength + 23) > TWEET_MAX_LENGTH;
    }

}
