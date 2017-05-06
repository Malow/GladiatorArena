Rails.application.routes.draw do
	root "main#index"
	get "/main" => "main#index"
  
	get "/login" => "main#login"
	post "/login" => "main#login"
  
	get "/register" => "main#register"
	post "/register" => "main#register"
  
	get "/create_user" => "main#create_user"
	post "/create_user" => "main#create_user"
	
	get "/logout" => "main#logout"
	
	post "/joinqueue" => "main#join_queue"
	post "/leavequeue" => "main#leave_queue"
end
