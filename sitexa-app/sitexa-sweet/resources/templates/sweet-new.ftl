<#import "template.ftl" as layout />

<@layout.mainLayout title="New sweet">

<form class="pure-form-stacked" action="/sweet-new" method="post" enctype="multipart/form-data">
    <input type="hidden" name="date" value="${date?c}">
    <input type="hidden" name="code" value="${code}">

    <label for="post-file">File:
        <input type="file" name="file1">
    </label>

    <label for="post-text">Text:
        <textarea id="post-text" name="text" rows="30" cols="100"></textarea>
    </label>

    <input class="pure-button pure-button-primary" type="submit" value="Post">

</form>

</@layout.mainLayout>