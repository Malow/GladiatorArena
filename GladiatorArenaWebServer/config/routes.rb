Rails.application.routes.draw do
	root "main#index"
	get "/main" => "main#index"
  
	get "/login" => "main#login"
	post "/login" => "main#login"
  
	get "/register" => "main#register"
	post "/register" => "main#register"
  
	get "/create_player" => "main#create_player"
	post "/create_player" => "main#create_player"
	
	get "/logout" => "main#logout"
	
	post "/joinqueue" => "main#join_queue"
	post "/leavequeue" => "main#leave_queue"
end
