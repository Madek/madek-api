require "spec_helper"

context "users" do
  context "admin user" do
    include_context :json_roa_client_for_authenticated_admin_user do
      before :each do
        @person = client.get.relation("people").post do |req|
          req.body = { last_name: "test",
                       subtype: "Person" }.to_json
          req.headers["Content-Type"] = "application/json"
        end.response.body.with_indifferent_access
      end

      describe "creating" do
        describe "a user" do
          it "works" do
            expect(client.get.relation("users").post do |req|
              req.body = { login: "test",
                           person_id: @person[:id] }.to_json
              req.headers["Content-Type"] = "application/json"
            end.response.status).to be == 201
          end
        end
      end

      describe "a via post created user" do
        let :created_user do
          client.get.relation("users").post do |req|
            req.body = { login: "test",
                         person_id: @person[:id] }.to_json
            req.headers["Content-Type"] = "application/json"
          end
        end
        describe "the data" do
          it "has the proper type" do
            expect(created_user.data["login"]).to be == "test"
          end
        end
        describe "the json-roa-data" do
          it "lets us navigate to the user via the self-relation" do
            expect(created_user.json_roa_data["self-relation"]["href"]).to \
              match /#{created_user.data["id"]}/
          end
        end
      end
    end
  end
end
