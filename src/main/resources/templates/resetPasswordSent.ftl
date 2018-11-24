<#include 'includes/head.ftl'>

<div class="container-fluid">

	<div class="row">
		<div class="col-xs-8">
			<h3>Squadlist - ${instance.name}</h3>
		</div>
	</div>
	<hr/>
		
	<div class="row">		
		<div class="col-xs-12">	
			<h4>$text.text('resetting.your.password')</h4>
			
			<p>An email containing password reset instructions has been sent to your email address.</p>
			<p>It should arrive within a few minutes.
			<p>If you do not receive this email, please contact your coach for assistance.</p>   
		</div>		
	</div>
	
	<div class="control-group">
		<div class="controls">		
			<a href="${urlBuilder.loginUrl()}"><button class="btn" type="button">$text.text('return.to.login.screen')</button></a>
		</div>
	</div>
	<hr/>	
</div>

<#include 'includes/footer.ftl'>