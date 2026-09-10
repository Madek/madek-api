require "spec_helper"

describe "filtering collections by responsible_delegation" do
  let :collections_relation do
    json_roa_client.get.relation("collections")
  end

  def get_collections(filter = nil)
    collections_relation.get(filter).data["collections"]
  end

  it "returns only collections responsible to the given delegation" do
    delegation = FactoryBot.create(:delegation)
    other_delegation = FactoryBot.create(:delegation)

    matching_collection = FactoryBot.create(
      :collection,
      responsible_user: nil,
      responsible_delegation: delegation,
      get_metadata_and_previews: true,
    )
    FactoryBot.create(
      :collection,
      responsible_user: nil,
      responsible_delegation: other_delegation,
      get_metadata_and_previews: true,
    )
    FactoryBot.create(
      :collection,
      responsible_user: FactoryBot.create(:user),
      get_metadata_and_previews: true,
    )

    filter = {
      permissions: [{ key: "responsible_delegation", value: delegation.id }],
    }
    fetched_collections = get_collections(
      "filter_by" => filter.deep_stringify_keys.to_json,
    )

    expect(fetched_collections.map { |c| c["id"] }).to eq [matching_collection.id]
  end
end
