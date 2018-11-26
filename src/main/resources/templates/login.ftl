<#include 'includes/head.ftl'>

<div class="container-fluid">

	<div class="row">
		<div class="col-xs-8">
			<h3>Squadlist - ${instance.name!}</h3>
		</div>
	</div>
	<hr/>
	<div class="row">
		<div class="col-xs-12">
		   	<form action="${urlBuilder.loginUrl()}" method="POST" class="form-horizontal" accept-charset="UTF-8">
				<div class="form-group">
				    <label for="username" class="col-sm-2 control-label"><@spring.message "username"/></label>
				    <div class="col-sm-6">
				        <input type="text" class="form-control" name="username" placeholder="<@spring.message "username"/>" value="${username!}">
				    </div>
				</div>
				        
		   	  	<div class="form-group">
				    <label for="password" class="col-sm-2 control-label"><@spring.message "password"/></label>
				    <div class="col-sm-6">					
				        <input type="password" class="form-control" name="password" placeholder="<@spring.message "password"/>">
				       	<p><a href="${urlBuilder.applicationUrl('/reset-password')}"><@spring.message "forgotten.your.password"/></a></p>
				    </div>
				</div>

			        <div class="form-group">
				    <div class="col-sm-2"></div>
                                    <div class="col-sm-6">
                                        <input type="checkbox" name="_spring_security_remember_me" id="remember">                                        
                                        <label for="remember">Keep me signed in</label>
                                    </div>
                                </div>
								
				<div class="row">	
					<div class="col-sm-2">
					</div>			   	
				   	<div class="col-sm-6">					
						<#if errors>
					        <div id="alert" class="alert alert-warning"><@spring.message "incorrect.login"/></div>
						</#if>
					</div>					
				</div>
	
				<div class="control-group">
					<div class="col-sm-2">
					</div>	
					<div class="controls">
						<input type="submit" name="login" value="<@spring.message "login"/>" class="btn btn-primary" />
					</div>
				</div>
								   	            
		     </form>	     
		</div>		
	</div>
	
	<div class="row">
		<div class="col-sm-2"></div>
		<div class="col-sm-6">
			<p>
			<br/>				
			<p><a href="${urlBuilder.facebookSignin()}"><@spring.message "login.with.linked.facebook.account"/></a></p>
		</div>					
	</div>	
	
	<div class="row">
		<div class="col-sm-2"></div>
		<div class="col-sm-6">
			<p>
			<br/>
			<p><@spring.messageText "require.access", ["${instance.name}"] /></p>
			<p>
			</br>
		</div>
	</div>

     <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
      <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingOne">
          <h4 class="panel-title">
            <a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
              About your data and cookies
            </a>
          </h4>
        </div>
        <div id="collapseOne" class="panel-collapse collapse" role="tabpanel" aria-labelledby="headingOne">
          <div class="panel-body">
            <#include 'data.ftl'>
          </div>
        </div>
      </div>
    </div>

	<hr/>
	
</div>

<#include 'includes/footer.ftl'>