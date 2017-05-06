class MainController < ApplicationController
	before_action :reset_error
	attr_accessor :error, :username, :rating, :isSearchingForGame, :currentGameId
	
	def reset_error
		@error = nil
	end
	
	def index
		@error = params[:error]
		email = session[:email]
		authToken = session[:authToken]
		if not email.blank? and not authToken.blank?
			request = {'email' => email, 'authToken' => authToken}.to_json
			begin
				response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/getmyinfo", request)
				if response["result"]
					@username = response["username"]
					@rating = response["rating"]
					@isSearchingForGame = response["isSearchingForGame"]
					@currentGameId = response["currentGameId"]
				else
					if response["error"] == "No user found for account"
						redirect_to "/create_user"
					else
						@error = response["error"]
					end
				end
			rescue => e
				redirect_to "/login"
			end
		else
			redirect_to "/login"
		end
	end
  
	def login
		if request.post?
			email = params[:email]
			password = params[:password]
			request = {'email' => email, 'password' => password}.to_json
			response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/account/login", request)
			if response["result"]
				session[:email] = email
				session[:authToken] = response["authToken"]
				redirect_to "/main"
			else
				@error = response["error"]
			end
		end
	end
	
	def register
		if request.post?
			email = params[:email]
			password = params[:password]
			request = {'email' => email, 'password' => password}.to_json
			response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/account/register", request)
			if response["result"]
				session[:email] = email
				session[:authToken] = response["authToken"]
				redirect_to "/main"
			else
				@error = response["error"]
			end
		end
	end
	
	def create_user
		if request.post?
			username = params[:username]
			email = session[:email]
			authToken = session[:authToken]
			if not email.blank? and not authToken.blank?
				request = {'email' => email, 'authToken' => authToken, 'username' => username}.to_json
				response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/createuser", request)
				if response["result"]
					redirect_to "/main"
				else
					@error = response["error"]
				end
			else
				redirect_to "/login"
			end
		end
	end
	
	def logout
		session[:email] = nil
		session[:authToken] = nil
		redirect_to "/login"
	end
	
	def join_queue
		email = session[:email]
		authToken = session[:authToken]
		if not email.blank? and not authToken.blank?
			request = {'email' => email, 'authToken' => authToken}.to_json
			response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/queuematchmaking", request)
			if not response["result"]
				@error = response["error"]
			end
		end
		redirect_to action: "index", error: @error
	end
	
	def leave_queue
		email = session[:email]
		authToken = session[:authToken]
		if not email.blank? and not authToken.blank?
			request = {'email' => email, 'authToken' => authToken}.to_json
			response = JSON.parse(RestClient.post "https://malow.duckdns.org:7000/unqueuematchmaking", request)
			if not response["result"]
				@error = response["error"]
			end
		end
		redirect_to action: "index", error: @error
	end
end
