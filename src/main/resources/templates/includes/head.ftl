<#import "/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
<head>
	<script src="//code.jquery.com/jquery-1.10.2.js"></script>
	<script src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
	<script src="$urlBuilder.staticUrl('jquery.ui.touch-punch.js')"></script>	

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css">
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

	<script src="$urlBuilder.staticUrl('bootstrap-colorselector/bootstrap-colorselector.js')"></script>
	<link href="$urlBuilder.staticUrl('bootstrap-colorselector/bootstrap-colorselector.css')" rel="stylesheet" type="text/css" />
	
	<title>Squadlist - ${title!}</title>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<#if rssUrl??>
        <link rel="alternate" type="application/rss+xml" href="${rssUrl}">
	</#if>
</head>
<body>

<#macro cancel to>
	<a href="${to}"><button class="btn" type="button"><@spring.message 'cancel' /></button></a>
</#macro>
