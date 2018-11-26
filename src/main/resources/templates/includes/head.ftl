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

<#macro navTabs selectedTab>
	<nav class="navbar navbar-default" role="navigation">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
	    		</button>
			</div>
			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
				<ul class="nav navbar-nav">
		    		<li <#if selectedTab == 'My outings'> class="active" </#if>>
		    			<a href="${urlBuilder.applicationUrl('/')}"><@spring.message 'my.outings' />
		    				<span id="pendingOutings" class="badge">${pendingOutingsCount!}</span>
		 	 	  		</a>
			    	</li>
			    	<li>
			    		<a href="${urlBuilder.applicationUrl('/member/')}${loggedInUser}/edit"><@spring.message 'my.details'/>
			    			<span id="memberDetailsProblems" class="badge">${memberDetailsProblems!}</span>
			    		</a>
			    	</li>
			    	<li <#if selectedTab == 'Outings'> class="active" </#if>><a href="${urlBuilder.outingsUrl(preferredSquad)}"><@spring.message 'outings'/></a></li>
					<li <#if selectedTab == 'Availability'> class="active" </#if>><a href="${urlBuilder.availabilityUrl(preferredSquad)}"><@spring.message 'availability' /></a></li>
					<li <#if selectedTab == 'Contacts'> class="active" </#if>><a href="${urlBuilder.contactsUrl(preferredSquad)}"><@spring.message 'contacts' /></a></li>
					<#if permissionsHelper.hasPermission('VIEW_ENTRY_DETAILS') >
						<li <#if selectedTab == 'Entry details'> class="active" </#if> ><a  href="${urlBuilder.entryDetails(preferredSquad)}" ><@spring.message 'entry.details' /></a></li>
					</#if>
					<#if permissionsHelper.hasPermission('VIEW_ADMIN_SCREEN') >
                        <li <#if selectedTab == 'Admin'> class="active" </#if>><a href="${urlBuilder.adminUrl()}"><@spring.message 'admin'/></a></li>
                    </#if>
			   	</ul>
			   	<ul class="nav navbar-nav navbar-right">
		        	<li class="dropdown">
		        		<a href="#" class="dropdown-toggle" data-toggle="dropdown">
		        			<span class="glyphicon glyphicon-cog" style="color: black"></span>
	    			   			<b class="caret"></b>
	    			   	</a>
	    			   	<ul class="dropdown-menu">
							<li><a href="${urlBuilder.changePassword()}"><@spring.message 'change.password'/></a></li>
							<li><a href="${urlBuilder.socialMediaAccounts()}"><@spring.message 'social.accounts'/></a></li>
							<li><a href="${urlBuilder.applicationUrl('/logout')}"><@spring.message 'logout'/></a></li>
						</ul>
					</li>
				</ul>
			</div>
		</div>
	</nav>
</#macro>