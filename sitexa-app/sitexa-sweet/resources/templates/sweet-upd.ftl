<#import "template.ftl" as layout />

<@layout.mainLayout title="Update sweet">
<form class="pure-form-stacked" action="/sweet-upd" method="post" enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="id" value="${sweet.id}">
    <input type="hidden" name="date" value="${date?c}">
    <input type="hidden" name="code" value="${code}">

    <label for="post-text">Text:
        <textarea id="post-text" name="text" rows="30" cols="100">${sweet.text}</textarea>
    </label>

    <input class="pure-button pure-button-primary" type="submit" value="Post">

</form>
</@layout.mainLayout>