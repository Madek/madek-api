require "spec_helper"

context "a user in a group" do
  before :each do
    @group = FactoryBot.create :institutional_group
    @user = FactoryBot.create :user, institutional_id: SecureRandom.uuid
    @group.users << @user
  end

  context "admin user" do
    include_context :json_roa_client_for_authenticated_admin_user do
      describe "getting the the user via the group with json roa" do
        it "works" do
          expect(
            client.get.relation("group").get(id: @group.id).relation("user").get(user_id: @user.id).response.status
          ).to be == 200
        end
        it "returns the expected properties" do
          expect(
            client.get.relation("group").get(id: @group.id).relation("user").get(user_id: @user.id).data
              .slice(:id, :email, :login, :institutional_id, :person_id)
          ).to be == @user.slice(:id, :email, :login, :institutional_id, :person_id)
        end
        it "contains the json-roa data to navigate further, e.g. self even if we naviage with the email" do
          expect(
            client.get.relation("group").get(id: @group.id).relation("user").get(user_id: @user.email).self_relation.get().data[:id]
          ).to be == @user[:id]
        end
      end

      describe "getting the the user via the group over bare http" do
        describe " via native ids " do
          let :faraday_response do
            client.conn.get("/api/groups/#{@group.id}/users/#{@user.id}")
          end

          it "works" do
            expect(faraday_response.status).to be == 200
          end

          it "has the expected properties" do
            expect(
              faraday_response.body.with_indifferent_access.slice(:id, :email, :login, :institutional_id, :person_id)
            ).to be == @user.slice(:id, :email, :login, :institutional_id, :person_id)
          end
        end

        describe " via institutional ids " do
          it "works" do
            url = "/api/groups/#{CGI.escape(@group.institutional_id)}" \
            "/users/#{CGI.escape(@user.institutional_id)}"
            expect(client.conn.get(url).status).to be == 200
          end
        end

        describe " via email " do
          it "works" do
            url = "/api/groups/#{CGI.escape(@group.institutional_id)}" \
            "/users/#{CGI.escape(@user.email)}"
            expect(client.conn.get(url).status).to be == 200
          end
        end
      end

      describe " using the wrong id (email) " do
        it "returns 404" do
          url = "/api/groups/#{CGI.escape(@group.institutional_id)}" \
          "/users/#{CGI.escape("noexists@nowhere")}"
          expect(client.conn.get(url).status).to be == 404
        end
      end
    end
  end
end
