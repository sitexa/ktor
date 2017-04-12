<#import "template.ftl" as layout />

<@layout.mainLayout title="View sweet">

<section class="post">
    <header class="post-header">
        <p class="post-meta">
            <span>${sweet.date.toDate()?string("yyyy.MM.dd HH:mm:ss")}</span>
            by ${sweet.userId}</p>
    </header>
    <div class="post-description">${sweet.text}</div>
    <section class="post">
        <div class="post-description">
            <#list replies as reply>
                <p class="post-meta">
                ${reply.date.toDate()?string("yyyy.MM.dd HH:mm:ss")} by ${reply.userId}</p>
            ${reply.text}
            </#list>
        </div>
    </section>
</section>

    <#if user??>
        <#if user.userId==sweet.userId>
        <p>
            <a href="javascript:void(0)" onclick="document.getElementById('editForm').submit()">Edit </a>
            <a href="javascript:void(0)" onclick="document.getElementById('deleteForm').submit()">Delete </a>
        </p>

        <form id="deleteForm" method="post" action="/sweet-del" enctype="application/x-www-form-urlencoded">
            <input type="hidden" name="id" value="${sweet.id}">
            <input type="hidden" name="date" value="${date?c}">
            <input type="hidden" name="code" value="${code}">
        </form>
        <form id="editForm" method="get" action="/sweet-upd" enctype="application/x-www-form-urlencoded">
            <input type="hidden" name="id" value="${sweet.id}">
            <input type="hidden" name="date" value="${date?c}">
            <input type="hidden" name="code" value="${code}">
        </form>
        </#if>
    <p>
        <a href="javascript:void(0)" onclick="document.getElementById('replyForm').submit()">Reply </a>
    </p>
    <form id="replyForm" method="get" action="/sweet-reply" enctype="application/x-www-form-urlencoded">
        <input type="hidden" name="replyTo" value="${sweet.id}">
        <input type="hidden" name="date" value="${date?c}">
        <input type="hidden" name="code" value="${code}">
    </form>
    </#if>

</@layout.mainLayout>