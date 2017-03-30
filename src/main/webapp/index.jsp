<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<jsp:directive.page isELIgnored="false"/> 
<!DOCTYPE html>
<html>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css" />

<!-- Optional theme -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css" />

<script src="http://code.jquery.com/jquery.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" ></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Interrogazione</title>
</head>
<body>
	<div id="exform" class="container-fluid ex">
		<h2>Inserisci la query nel linguaggio che conosci</h2>
		<div class="col-md-4 col-md-offset-4">
			<div class="form_container">
				<form action="controllerQuery" method="post">
					<div class="form-group">
						<label>Query SQL</label> <input type="text" class="form-control" name="querySQL">
					</div>
					<div class="form-group">
						<label>Query MongoDB</label> <input type="text" class="form-control" name="queryMongoDB">
					</div>
					<div class="form-group">
						<label>Query Cypher</label><input type="text" class="form-control" name="queryCypher">
					</div>
					<button type="submit" class="btn btn-default">Invia</button>
					<p>
					${queryError}
					</p>
				</form>
			</div>
		</div>
	</div>
</body>
</html>
