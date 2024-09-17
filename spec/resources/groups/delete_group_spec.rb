require "spec_helper"

context "groups" do
  before :each do
    @group = FactoryBot.create :group
  end

  context "non admin user" do
    include_context :json_roa_client_for_authenticated_user do
      it "is forbidden to delete any group" do
        expect(
          client.get.relation("group").delete(id: @group.id).response.status
        ).to be == 403
      end
    end
  end

  context "admin user" do
    include_context :json_roa_client_for_authenticated_admin_user do
      context "deleting a standard group" do
        let :delete_group_result do
          client.get.relation("group").delete(id: @group.id)
        end

        it "returns the expected status code 204" do
          expect(delete_group_result.response.status).to be == 204
        end

        it "effectively removes the group" do
          expect(delete_group_result.response.status).to be == 204
          expect(Group.find_by(id: @group.id)).not_to be
        end
      end
    end
  end
end
