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
			<p>$text.text('reset.password.instructions')</p>
		</div>
	</div>
	
	<div class="row">
		<div class="col-xs-12">
			<form method="POST" action="${urlBuilder.resetPassword()}" class="form-horizontal" accept-charset="UTF-8" autocomplete="off">
				<div class="form-group">
				    <label for="currentPassword" class="col-sm-3 control-label">$text.text('username.or.email.address')</label>
				    <div class="col-sm-9">
				    	<input class="form-control" name="username" placeholder="$text.text('username.or.email.address')">				      				      					   				
				    </div>
				</div>

				<div class="row">	
					<div class="col-sm-3">
					</div>			   	
				   	<div class="col-sm-6">
						<#if errors>
					        <div id="alert" class="alert alert-warning">$text.text('unknown.username.or.email.address')</div>
						</#if>
					</div>					
				</div>
																					
				<div class="control-group">
					<div  class="col-sm-3"></div><div class="controls">
						<button type="submit" class="btn btn-primary">$text.text('reset.password')</button>
						#cancel($urlBuilder.loginUrl())
					</div>
				</div>				
			</form>
		</div>
	</div>
	<hr/>	
</div>

<#include 'includes/footer.ftl'>