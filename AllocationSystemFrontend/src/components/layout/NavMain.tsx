import { ChevronRight, type LucideIcon } from "lucide-react"
import { Link, useLocation } from "react-router-dom"

import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuAction,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from "@/components/ui/sidebar"

export type NavItem = {
  title: string
  url: string
  icon: LucideIcon
  isActive?: boolean
  items?: {
    title: string
    url: string
  }[]
}

export type NavGroup = {
  label: string
  items: NavItem[]
}

export function NavMain({
  groups,
}: {
  groups: NavGroup[]
}) {
  const location = useLocation()

  const isItemActive = (item: NavItem) => {
    if (item.isActive !== undefined) {
      return item.isActive
    }
    if (item.items?.length) {
      return item.items.some((subItem) => location.pathname === subItem.url)
    }
    return location.pathname === item.url
  }

  const isSubItemActive = (subItemUrl: string) => {
    return location.pathname === subItemUrl
  }

  // Find the first item with subitems to expand by default
  const getFirstDropdownIndex = (groups: NavGroup[]): number | null => {
    let itemIndex = 0
    for (const group of groups) {
      for (const item of group.items) {
        if (item.items && item.items.length > 0) {
          return itemIndex
        }
        itemIndex++
      }
    }
    return null
  }

  const firstDropdownIndex = getFirstDropdownIndex(groups)
  let currentItemIndex = 0

  return (
    <>
      {groups.map((group) => (
        <SidebarGroup key={group.label}>
          <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
          <SidebarMenu>
            {group.items.map((item) => {
              const hasSubItems = item.items && item.items.length > 0
              const itemActive = isItemActive(item)
              const isFirstDropdown = currentItemIndex === firstDropdownIndex
              const shouldDefaultOpen = itemActive || (isFirstDropdown && hasSubItems)
              
              currentItemIndex++
              
              return (
                <Collapsible key={item.title} asChild defaultOpen={shouldDefaultOpen}>
                  <SidebarMenuItem>
                    <SidebarMenuButton
                      asChild
                      tooltip={item.title}
                      isActive={!hasSubItems && itemActive}
                    >
                      <Link to={item.url}>
                        <item.icon />
                        <span>{item.title}</span>
                      </Link>
                    </SidebarMenuButton>
                    {hasSubItems ? (
                      <>
                        <CollapsibleTrigger asChild>
                          <SidebarMenuAction className="data-[state=open]:rotate-90">
                            <ChevronRight />
                            <span className="sr-only">Toggle</span>
                          </SidebarMenuAction>
                        </CollapsibleTrigger>
                        <CollapsibleContent>
                          <SidebarMenuSub>
                            {item.items?.map((subItem) => (
                              <SidebarMenuSubItem key={subItem.title}>
                                <SidebarMenuSubButton
                                  asChild
                                  isActive={isSubItemActive(subItem.url)}
                                >
                                  <Link to={subItem.url}>
                                    <span>{subItem.title}</span>
                                  </Link>
                                </SidebarMenuSubButton>
                              </SidebarMenuSubItem>
                            ))}
                          </SidebarMenuSub>
                        </CollapsibleContent>
                      </>
                    ) : null}
                  </SidebarMenuItem>
                </Collapsible>
              )
            })}
          </SidebarMenu>
        </SidebarGroup>
      ))}
    </>
  )
}
