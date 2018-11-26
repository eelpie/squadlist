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
			<h4><@spring.message 'resetting.your.password' /></h4>
			
			<p><@spring.message 'your.new.password.is' /><code>$newPassword</code></p>
		</div>		
	</div>
	
	<div class="control-group">
		<div class="controls">		
			<a href="${urlBuilder.loginUrl()}"><button class="btn" type="button"><@spring.message 'return.to.login.screen' /></button></a>
		</div>
	</div>
	<hr/>	
</div>

<#include 'includes/footer.ftl'>