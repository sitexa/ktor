<#import "template.ftl" as layout />

<@layout.mainLayout title="Reply sweet">
<section class="post">
    <header class="post-header">
        <p class="post-meta">
            <span>${sweet.date.toDate()?string("yyyy.MM.dd HH:mm:ss")}</span>
            by ${sweet.userId}</p>
    </header>
    <div class="post-description">${sweet.text}</div>
</section>

<form class="pure-form-stacked" action="/sweet-reply" method="post" enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="replyTo" value="${sweet.id}">
    <input type="hidden" name="date" value="${date?c}">
    <input type="hidden" name="code" value="${code}">

    <label for="post-text">Text:
        <textarea id="post-text" name="text" rows="30" cols="100"></textarea>
    </label>

    <input class="pure-button pure-button-primary" type="submit" value="Post">

</form>

</@layout.mainLayout>