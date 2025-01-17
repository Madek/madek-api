require "spec_helper"

context "delgations" do
  before :each do
    @delegation = FactoryBot.create :delegation
  end

  context "non admin user" do
    include_context :json_roa_client_for_authenticated_user do
      let :get_delegations_result do
        client.get.relation("delegation").get(id: @delegation.id)
      end

      it "responses with 200" do
        expect(get_delegations_result.response.status).to be == 200
      end

      it "contains the proper data" do
        expect(
          get_delegations_result.data.with_indifferent_access.slice(:id, :name, :description)
        ).to eq(
          @delegation.attributes.with_indifferent_access.slice(:id, :name, :description)
        )
      end

      it "does not contain sensitive data" do
        expect(get_delegations_result.data.with_indifferent_access).not_to have_key(:admin_comment)

        expect(get_delegations_result.data.with_indifferent_access).not_to have_key(:notifications_email)
      end
    end
  end
end
