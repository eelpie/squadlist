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
			<div class="alert alert-danger">Invalid reset token</div>
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