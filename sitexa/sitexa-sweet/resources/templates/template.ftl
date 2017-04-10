<#-- @ftlvariable name="user" type="com.sitexa.sweet.model.User" -->

<#macro mainLayout title="Welcome">
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <title>${title} | Sweet</title>
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/pure-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css">
    <link rel="stylesheet" type="text/css" href="/styles/main.css">
</head>
<body>
<div class="pure-g">
    <div class="sidebar pure-u-1 pure-u-md-1-4">
        <div class="header">
            <div class="brand-title">Sweet</div>
            <nav class="nav">
                <ul class="nav-list">
                    <#if user??>
                        <li class="nav-item"><a class="pure-button" href="/">home</a></li>
                        <li class="nav-item"><a class="pure-button" href="/user/${user.userId}">my timeline</a></li>
                        <li class="nav-item"><a class="pure-button" href="/sweet-new">New sweet</a></li>
                        <li class="nav-item"><a class="pure-button" href="/logout">sign out
                            [${user.displayName?has_content?then(user.displayName, user.userId)}]</a></li>
                    <#else>
                        <li class="nav-item"><a class="pure-button" href="/register">sign up</a></li>
                        <li class="nav-item"><a class="pure-button" href="/login">sign in</a></li>
                    </#if>
                </ul>
            </nav>
        </div>
    </div>

    <div class="content pure-u-1 pure-u-md-3-4">
        <h2>${title}</h2>
        <#nested />
    </div>
    <div class="footer">
        Sitexa Sweetie, ${.now?string("yyyy")}
    </div>
</div>
</body>
</html>
</#macro>

<#-- @ftlvariable name="sweet" type="java.util.List<com.sitexa.sweet.model.Sweet>" -->
<#macro sweet_li sweet>
<section class="post">
    <header class="post-header">
        <p class="post-meta">
            <a href="/sweet/${sweet.id}">${sweet.date.toDate()?string("yyyy.MM.dd HH:mm:ss")}</a>
            by ${sweet.userId}</p>
    </header>
    <div class="post-description">${sweet.text}</div>
</section>
</#macro>

<#macro sweets_list sweets>
<ul>
    <#list sweets as sweet>
        <@sweet_li sweet=sweet></@sweet_li>
    <#else>
        <li>There are no sweets yet</li>
    </#list>
</ul>
</#macro>